/*
 * System calls for Files
 * Added by:Pratik
 */
#include <types.h>
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
#include <addrspace.h>




int
init_filetable(struct file_handle **fd_table){

	//Assigning null values
	for(int i=0;i<OPEN_MAX;i++){
		fd_table[i]=NULL;
	}

	int result=0;
	struct vnode* v1;
	char con1[5]={0};

	// console file : stdin

	strcpy(con1,"con:");
	result=vfs_open(con1,O_RDONLY,0664,&v1);
	if(result){
		return (result);
	}
	fd_table[0]=(struct file_handle*)kmalloc(sizeof(struct file_handle));
	if(fd_table[0] == NULL) {
		vfs_close(v1);
		return ENOMEM;
	}
	fd_table[0]->vnode=v1;
	fd_table[0]->flags=O_RDONLY;
	strcpy(fd_table[0]->filename,con1);
	fd_table[0]->references=1;
	fd_table[0]->filelock = lock_create(fd_table[0]->filename);
	if(fd_table[0]->filelock == NULL) {
		vfs_close(v1);
		kfree(fd_table[0]);
		return ENOMEM;
	}

	// console file : stdout
	char con2[5]={0};
	struct vnode* v2;
	strcpy(con2,"con:");
	result=vfs_open(con2,O_WRONLY,0664,&v2);

	if(result){
		vfs_close(v1);
		lock_destroy(fd_table[0]->filelock);
		kfree(fd_table[0]);
		return (result);
	}
	fd_table[1]=(struct file_handle*)kmalloc(sizeof(struct file_handle));
	if(fd_table[1] == NULL) {
		vfs_close(v1);
		vfs_close(v2);
		lock_destroy(fd_table[0]->filelock);
		kfree(fd_table[0]);
		return ENOMEM;
	}
	fd_table[1]->vnode=v2;
	fd_table[1]->flags=O_WRONLY;
	strcpy(fd_table[1]->filename,con2);
	fd_table[1]->references=1;
	fd_table[1]->filelock = lock_create(fd_table[1]->filename);
	if(fd_table[1]->filelock == NULL) {
		vfs_close(v1);
		vfs_close(v2);
		lock_destroy(fd_table[0]->filelock);
		kfree(fd_table[0]);
		kfree(fd_table[1]);
		return ENOMEM;
	}

	// console file : stderr

	char con3[5]={0};
	struct vnode* v3;
	strcpy(con3,"con:");
	result=vfs_open(con3,O_WRONLY,0664,&v3);
	if(result){
		vfs_close(v1);
		vfs_close(v2);
		lock_destroy(fd_table[0]->filelock);
		lock_destroy(fd_table[1]->filelock);
		kfree(fd_table[0]);
		kfree(fd_table[1]);
		return (result);
	}
	fd_table[2]=(struct file_handle*)kmalloc(sizeof(struct file_handle));
	if(fd_table[2] == NULL) {
		vfs_close(v1);
		vfs_close(v2);
		vfs_close(v3);
		lock_destroy(fd_table[0]->filelock);
		lock_destroy(fd_table[1]->filelock);
		kfree(fd_table[0]);
		kfree(fd_table[1]);
		return ENOMEM;
	}
	fd_table[2]->vnode=v3;
	fd_table[2]->flags=O_WRONLY;
	strcpy(fd_table[2]->filename,con3);
	fd_table[2]->references=1;
	fd_table[2]->filelock = lock_create(fd_table[2]->filename);
	if(fd_table[2]->filelock == NULL) {
		vfs_close(v1);
		vfs_close(v2);
		vfs_close(v3);
		lock_destroy(fd_table[0]->filelock);
		lock_destroy(fd_table[1]->filelock);
		kfree(fd_table[0]);
		kfree(fd_table[1]);
		kfree(fd_table[2]);
		return ENOMEM;
	}

	return result;
}

int
sys_open(char *fname,int flag,int *retval){

	// fileststat to know the size of file for offset

	struct stat filestat;

	int result=0;
	// Checking for valid filename
	if(fname==NULL){
		return (EFAULT);
	}


	if(result){
		return (result);
	}

	char dest[PATH_MAX];
	size_t len;

	//copying to kernel space
	int copied=copyinstr((const_userptr_t)fname,dest,PATH_MAX,&len);

	if(copied!=0){
		return (copied);
	}

	// Path too long
	if (strlen(dest) > PATH_MAX ) {

		return (ENAMETOOLONG);
	}


	//Checking filetable for an empty node

	int index=0;
	while(index<OPEN_MAX){
		if(curthread->fd_table[index]==NULL){
			curthread->fd_table[index]=(struct file_handle*)kmalloc(sizeof(struct file_handle));
			if(curthread->fd_table[index] == NULL) {
				return ENOMEM;
			}
			strcpy(curthread->fd_table[index]->filename,dest);
			curthread->fd_table[index]->references=1;
			curthread->fd_table[index]->flags=flag;
			curthread->fd_table[index]->filelock = lock_create(curthread->fd_table[index]->filename);
			if(curthread->fd_table[index]->filelock == NULL) {
				kfree(curthread->fd_table[index]);
				curthread->fd_table[index] = NULL;
				return ENOMEM;
			}
			break;
		}

		index++;
	}

	//Too many open files

	if(index >= OPEN_MAX){
		return (ENFILE);
	}

	//Opening the file
	result=vfs_open(fname,flag,0,&curthread->fd_table[index]->vnode);
	if(result){
		lock_destroy(curthread->fd_table[index]->filelock);
		kfree(curthread->fd_table[index]);
		curthread->fd_table[index] = NULL;
		return (result);
	}

	//Assigning the offset
	if(flag==O_APPEND){

		if((result=VOP_STAT(curthread->fd_table[index]->vnode,&filestat))!=0){
			lock_destroy(curthread->fd_table[index]->filelock);
			kfree(curthread->fd_table[index]);
			curthread->fd_table[index] = NULL;
			return (result);
		}
		else
			curthread->fd_table[index]->offset=filestat.st_size;

	} else{
		curthread->fd_table[index]->offset=0;
	}

	*retval=index;
	return (0);
}


int
sys_write(int fd, void *buf, size_t buflen, int *retval) {

	int result;
	size_t actual_len=0;


	char*check;
	check=kmalloc(sizeof(char));
	result=copyinstr((const userptr_t)buf,check,NAME_MAX,&actual_len);
	kfree(check);
	if(result){

		return result;
	}

	//	check if fd is a valid file descriptor
	if (fd < 0 || fd >= OPEN_MAX || (curthread->fd_table[fd] == NULL)) {
		return EBADF;
	}

	//	create structs

	struct iovec iov;
	struct uio u;

	lock_acquire(curthread->fd_table[fd]->filelock);

	//	initialize structs
	iov.iov_ubase = buf;
	iov.iov_len = buflen;
	u.uio_iov = &iov;
	u.uio_iovcnt = 1;
	u.uio_offset = curthread->fd_table[fd]->offset;
	u.uio_resid = buflen;
	u.uio_segflg = UIO_USERSPACE;
	u.uio_rw = UIO_WRITE;
	u.uio_space = curthread->t_addrspace;

	//	write to disk
	result = VOP_WRITE(curthread->fd_table[fd]->vnode, &u);

	//	set number of bytes written to retval
	if(result == 0) {
		curthread->fd_table[fd]->offset = u.uio_offset;
		*retval = buflen - u.uio_resid;
	}

	lock_release(curthread->fd_table[fd]->filelock);

	//	return either error code or 0 for success
	return result;
}

int
sys_read(int fd, void *buf, size_t buflen, int *retval) {
	int result;
	size_t actual_len=0;


	char*check;
	check=kmalloc(sizeof(char));
	result=copyinstr((const userptr_t)buf,check,NAME_MAX,&actual_len);
	kfree(check);
	if(result){

		return result;
	}

	//	check if fd is a valid file descriptor
	if (fd < 0 || fd >= OPEN_MAX || (curthread->fd_table[fd] == NULL)) {
		return EBADF;
	}
	//	create structs

	struct iovec iov;
	struct uio u;

	lock_acquire(curthread->fd_table[fd]->filelock);

	//	initialize structs
	iov.iov_ubase = buf;
	iov.iov_len = buflen;
	u.uio_iov = &iov;
	u.uio_iovcnt = 1;
	u.uio_offset = curthread->fd_table[fd]->offset;
	u.uio_resid = buflen;
	u.uio_segflg = UIO_USERSPACE;
	u.uio_rw = UIO_READ;
	u.uio_space = curthread->t_addrspace;
	//	read from disk
	result = VOP_READ(curthread->fd_table[fd]->vnode, &u);
	//	set number of bytes read to retval
	if(result == 0) {
		curthread->fd_table[fd]->offset = u.uio_offset;
		*retval = buflen - u.uio_resid;
	}

	lock_release(curthread->fd_table[fd]->filelock);

	//	return either error code or 0 for success
	return result;
}

int
sys_close(int fd) {

	//Checking for valid index of file descriptor

	if (fd < 0 || fd >= OPEN_MAX || (curthread->fd_table[fd] == NULL)) {
		return (EBADF);
	}

	//Checking for referenced files
	if(curthread->fd_table[fd]->references > 1){
		curthread->fd_table[fd]->references--;
		curthread->fd_table[fd] = NULL;
		return (0);
	}
	// If no thread referring to the file descriptor
	else
	{
		lock_destroy(curthread->fd_table[fd]->filelock);
		vfs_close(curthread->fd_table[fd]->vnode);
		kfree(curthread->fd_table[fd]);
		curthread->fd_table[fd] = NULL;
	}

	return(0);
}

off_t
sys_lseek(int fd, off_t pos, int whence, int *retval){
	int result=0;



	struct stat filestat;
	off_t file_size;

	//checking valid file descriptor
	if(fd < 0 || fd >= OPEN_MAX || curthread->fd_table[fd] == NULL || curthread->fd_table[fd]->filename==NULL){
		return(EBADF);
	}

	//check if lseek is called on console
	if(!strcmp(curthread->fd_table[fd]->filename, "con:")) {
		return ESPIPE;
	}

	//checking valid whence
	if(whence != SEEK_SET && whence != SEEK_CUR && whence != SEEK_END) {
		return(EINVAL);
	}

	//checking for valid position
	if(pos < 0){
		return(EINVAL);
	}

	lock_acquire(curthread->fd_table[fd]->filelock);

	if((result=VOP_STAT(curthread->fd_table[fd]->vnode,&filestat))!=0){
		lock_release(curthread->fd_table[fd]->filelock);
		return (result);
	}
	file_size=filestat.st_size;

	switch(whence) {
	case SEEK_SET:
		curthread->fd_table[fd]->offset = pos;
		break;
	case SEEK_CUR:
		curthread->fd_table[fd]->offset += pos;
		break;
	case SEEK_END:
		curthread->fd_table[fd]->offset =file_size + pos;
		break;
	}

	result = VOP_TRYSEEK(curthread->fd_table[fd]->vnode,curthread->fd_table[fd]->offset);
	if(result) {
		lock_release(curthread->fd_table[fd]->filelock);
		return (result);
	}

	*retval = curthread->fd_table[fd]->offset;

	lock_release(curthread->fd_table[fd]->filelock);

	return(0);

}


int
sys_dup2(int oldfd, int newfd,int *retval) {

	//checking for validity of newfd and oldfd
	if (newfd < 0 || oldfd < 0 || newfd >= OPEN_MAX || oldfd >= OPEN_MAX || (curthread->fd_table[oldfd] == NULL) ) {
		return (EBADF);
	}

	if(oldfd != newfd) {
		// checking newfd
		if(curthread->fd_table[newfd] != NULL) {
			sys_close(newfd);
		}

		//assign oldfd filehandle pointer value to newfd file handle pointer
		curthread->fd_table[newfd] = curthread->fd_table[oldfd];

		lock_acquire(curthread->fd_table[oldfd]->filelock);
		//increment references
		curthread->fd_table[oldfd]->references++;
		lock_release(curthread->fd_table[oldfd]->filelock);
	}

	*retval = newfd;
	return 0;
}

int
sys_chdir(const char *pathname){

	int result=0;
	char dest[PATH_MAX];
	size_t len;

	//copying pathname from user space to kernel space
	result=copyinstr((const_userptr_t)pathname,dest,PATH_MAX, &len);
	if (result) {
		return result;
	}
	result = vfs_chdir(dest);

	if(result) {
		return (result);
	}
	return(0);
}

int
sys__getcwd(char *buf, size_t buflen, int *retval){

	struct uio u;
	struct iovec iov;
	int result;
	size_t actual_len=0;


	char*check;
	check=kmalloc(sizeof(char));
	result=copyinstr((const userptr_t)buf,check,NAME_MAX,&actual_len);
	kfree(check);
	if(result){

		return result;
	}
	//checking valid buffer
	if(buf==NULL){
		return(EFAULT);
	}

	//initialize iovec and uio for kernel I/O
	uio_kinit(&iov, &u, buf, buflen, 0, UIO_READ);

	result = vfs_getcwd(&u);
	if(result) {
		return (result);
	}

	buf[sizeof(buf)-1 - u.uio_resid] = 0;

	*retval = strlen(buf);
	return(0);

}
