/*
 * process_syscalls.c
 *
 *  Created on: Mar 8, 2014
 *      Author: Pratik
 */

#include <types.h>
#include <synch.h>
#include <file_syscalls.h>
#include <syscall.h>
#include <clock.h>
#include <copyinout.h>
#include <limits.h>
#include <kern/types.h>
#include <kern/errno.h>
#include <types.h>
#include <vnode.h>
#include <uio.h>
#include <copyinout.h>
#include <current.h>
#include <thread.h>
#include <lib.h>
#include <vfs.h>
#include <syscall.h>
#include <kern/seek.h>
#include <kern/fcntl.h>
#include <kern/stat.h>
#include <kern/wait.h>
#include <addrspace.h>
#include <mips/trapframe.h>
#include <process_syscalls.h>


pid_t pid_alloc(void){
	int i;
	for(i=1;i<__PROC_MAX;i++){
		if(process_table[i] == NULL)
			break;
	}
	if(i == __PROC_MAX) {
		return -1;
	}
	pid_counter++;
	if(pid_counter == __PID_MAX) {
		return -2;
	}
	return i;
}

int init_process(int ptable_index){

	struct process *p;
	p = (struct process*)kmalloc(sizeof(struct process));
	if(p == NULL) {
		return ENOMEM;
	}
	p->exitcode=0;
	p->exitsem=sem_create("exitsem",0);
	if(p->exitsem == NULL) {
		kfree(p);
		return ENOMEM;
	}
	p->exited=false;
	p->ppid=0;
	process_table[ptable_index]=p;
	return 0;
}

static void destroy_process(int ptable_index) {
	sem_destroy(process_table[ptable_index]->exitsem);
	kfree(process_table[ptable_index]);
	process_table[ptable_index] = NULL;
}

int sys_getpid(int *retval){
	*retval = curthread->pid;
	return 0;
}

static int getptable_index(pid_t pid) {
	int i;
	for(i=0;i<__PROC_MAX;i++) {
		if(process_table[i] != NULL && process_table[i]->pid == pid) {
			return i;
		}
	}
	return -1;
}

pid_t sys_waitpid(pid_t pid, int *status, int options,int *retval){

	//checking for valid pid
	if(pid < 1 || pid >= __PID_MAX){
		return ESRCH;
	}
	int ptable_index = getptable_index(pid);
	if(ptable_index == -1 || process_table[ptable_index] == NULL) {
		return ESRCH;
	}

	//valid status address check
	if(status==NULL|| (vaddr_t) status <= 0x40000000 ||(vaddr_t)status % 4 !=0) {
		return EFAULT;
	}

	if(curthread->pid!=0 && (vaddr_t) status >= USERSPACETOP){
		return EFAULT;
	}

	if(process_table[ptable_index]->ppid != curthread->pid){
		return ECHILD;
	}


	switch(options){
	case WNOHANG:
		if(!process_table[ptable_index]->exited){
			*retval = 0;
			return 0;
		} else {
			*status =_MKWAIT_EXIT(process_table[ptable_index]->exitcode);
			destroy_process(ptable_index);
		}
		break;
	case 0:{
		if(process_table[ptable_index]->exited){
			*status =_MKWAIT_EXIT(process_table[ptable_index]->exitcode);
			destroy_process(ptable_index);
		}else{
			P(process_table[ptable_index]->exitsem);
			*status =_MKWAIT_EXIT(process_table[ptable_index]->exitcode);
			destroy_process(ptable_index);
		}
		break;
	}

	default:
		return EINVAL;
	}

	*retval = pid;
	return 0;
}

pid_t sys_fork(void (*enter_forked_process)(void *data1, unsigned long data2),
		void *parenttf, int *retval) {

	P(forkSem);

	struct trapframe *childtf = kmalloc(sizeof(struct trapframe));
	struct thread *child;
	int result;

	if(childtf == NULL) {
		V(forkSem);
		return ENOMEM;
	}

	memcpy(childtf, parenttf, sizeof(struct trapframe));

	result = thread_fork(curthread->t_name, enter_forked_process, childtf, (unsigned long)curthread->t_addrspace, &child);

	if(result) {
		V(forkSem);
		kfree(childtf);
	} else {
		*retval = child->pid;
	}

	return result;
}

void sys__exit(int exitcode){

	V(forkSem);
	V(execvSem);
	execvSem->sem_count = 2;

	process_table[curthread->ptable_index]->exitcode = exitcode;
	process_table[curthread->ptable_index]->exited = true;
	V(process_table[curthread->ptable_index]->exitsem);
	thread_exit();
}


int
sys_execv(char *progname, char **argv) {

	P(execvSem);

	int argc = 0;
	int result = 0;
	size_t actual_len = 0;
	vaddr_t entrypoint,stackptr;
	size_t len;
	size_t stackoffset = 0;
	struct vnode *vn = 0;
	char **kargv;
	char*check;


	if(argv==NULL){
		V(execvSem);
		return EFAULT;
	}

	check=kmalloc(sizeof(char));
	result=copyinstr((const userptr_t)argv,check,NAME_MAX,&actual_len);
	kfree(check);
	if(result){
		V(execvSem);
		return result;
	}

	//copying the file name
	char dest[PATH_MAX];
	result=copyinstr((const_userptr_t)progname,dest,PATH_MAX, &actual_len);

	if(result){
		V(execvSem);
		return result;
	}

	//opening the file
	result = vfs_open(dest, O_RDONLY, 0, &vn);

	if (result) {
		V(execvSem);
		return result;
	}

	kargv=(char **)kmalloc(512 * sizeof(char*));

	int kbufsize=0;

	//copying arguments to kernel buffer
	if(argv != NULL) {
		//len=0;
		int kbuffptr=0;

		while(argv[argc] != NULL)  {

			actual_len = 0;
			kargv[kbuffptr]=kmalloc(NAME_MAX * sizeof(char));
			result=copyinstr((const userptr_t)argv[argc],kargv[kbuffptr],NAME_MAX,&actual_len);
			if(result){
				V(execvSem);
				return result;
			}

			//copying padded arguments of length in multiple of 4
			argc ++;
			if(argc >= ARG_MAX) {
				V(execvSem);
				return E2BIG;
			}
			kbuffptr++;
		}
		kargv[kbuffptr]=NULL;
		kbufsize=kbuffptr;

	}

	// Creating new address space.

	struct addrspace *oldaddr = curthread->t_addrspace;
	struct addrspace *temp_as=as_create();

	if (temp_as==NULL) {
		V(execvSem);
		vfs_close(vn);
		return ENOMEM;
	}

	curthread->t_addrspace=temp_as;

	//activating address space
	as_activate(temp_as);

	result = load_elf(vn,&entrypoint);

	if (result) {
		V(execvSem);
		curthread->t_addrspace = oldaddr;
		vfs_close(vn);
		as_destroy(temp_as);
		return result;

	}

	//closing file now
	vfs_close(vn);

	// Defining the user stack in the address space

	stackptr = 0;
	result = as_define_stack(temp_as, &stackptr);
	if (result) {
		V(execvSem);
		curthread->t_addrspace = oldaddr;
		as_destroy(temp_as);
		return result;
	}

	as_destroy(oldaddr);

	vaddr_t argvptr[kbufsize];

	//copying arguments from buffer to stack

	for (int i =kbufsize-1; i>=0; i--)
	{
		if(kargv[i]!=0){
			len = strlen(kargv[i])+1;

			if(len>4){

				int l1 = len % 4;
				if(l1 == 1) {
					len += 3;
				}else if(l1==2){
					len+=2;
				}else if(l1==3){
					len+=1;
				}

			}else
				len=4;


			stackptr -= len;
			argvptr[i] = stackptr;
			copyout(kargv[i], (userptr_t) argvptr[i], len);
		}
	}
	argvptr[kbufsize] = 0;

	//freeing kernel buffer
	for(int i=0;i<kbufsize;i++){
		kfree(kargv[i]);
	}
	kfree(kargv);

	// Create space for the argument pointers
	stackoffset += sizeof(vaddr_t) * kbufsize*4;

	// Adjust the stack pointer and align it
	stackptr = stackptr - stackoffset;

	// Copy the argument pointers onto the stack
	copyout(argvptr, (userptr_t) stackptr, sizeof(vaddr_t) * (argc+1));

	// Entering user mode
	enter_new_process(argc, (userptr_t) stackptr, stackptr, entrypoint);

	panic("enter_new_process returned\n");
	return EINVAL;
}
