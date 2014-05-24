/*
 * process_syscalls.h
 *
 *  Created on: Mar 8, 2014
 *      Author: trinity
 */

#ifndef PROCESS_SYSCALLS_H_
#define PROCESS_SYSCALLS_H_

#include <limits.h>
#include <synch.h>

struct process {
	pid_t pid;
    pid_t ppid;
    struct semaphore* exitsem;
    bool exited;
    int exitcode;
    struct thread* self;
};

//initialized in thread_bootstrap
int pid_counter;
struct process *process_table[__PROC_MAX];
struct semaphore *forkSem;
struct semaphore *execvSem;

pid_t pid_alloc(void);
int sys_getpid(int *retval);
int init_process(pid_t pid);
pid_t sys_fork(void (*entrypoint)(void *data1, unsigned long data2),
				void *parenttf, int *retval);
pid_t sys_waitpid(pid_t pid, int *status, int options,int *retval);
void sys__exit(int exitcode);
int sys_execv(char *progname, char **argv);

#endif /* PROCESS_SYSCALLS_H_ */
