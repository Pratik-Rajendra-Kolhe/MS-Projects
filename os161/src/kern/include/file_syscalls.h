/*
 * file_syscalls.h
 *
 *  Created on: Mar 5, 2014
 *      Author: trinity
 */
/*
 * Header for file System calls
 * Added By: Pratik
 */
#ifndef FILE_SYSCALLS_H_
#define FILE_SYSCALLS_H_

#include<limits.h>
#include<types.h>
#include<vnode.h>
#include<synch.h>

struct file_handle{
	char filename[MAX_FILENAME_LEN];
	int flags;
    off_t offset;
    int references;
    struct lock* filelock;
    struct vnode *vnode;
};




int init_filetable(struct file_handle **fd_table);
int sys_open(char *fname,int flag,int *retval);
int sys_close(int fd);
int sys_write(int fd, void *buf, size_t nbytes,int *retval);
off_t sys_lseek(int fd, off_t pos, int whence,int *retval);
int sys_dup2(int oldfd, int newfd,int *retval);
int sys_chdir(const char *pathname);
int sys__getcwd(char *buf, size_t buflen,int *retval);
int sys_read(int fd, void *buf, size_t buflen, int *retval);

#endif /* FILE_SYSCALLS_H_ */
