/*
 * Copyright (c) 2000, 2001, 2002, 2003, 2004, 2005, 2008, 2009
 *	The President and Fellows of Harvard College.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the University nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE UNIVERSITY OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 */

/*
 * Synchronization primitives.
 * The specifications of the functions are in synch.h.
 */

#include <types.h>
#include <lib.h>
#include <spinlock.h>
#include <wchan.h>
#include <thread.h>
#include <current.h>
#include <synch.h>

////////////////////////////////////////////////////////////
//
// Semaphore.

struct semaphore *
sem_create(const char *name, int initial_count)
{
        struct semaphore *sem;

        KASSERT(initial_count >= 0);

        sem = kmalloc(sizeof(struct semaphore));
        if (sem == NULL) {
                return NULL;
        }

        sem->sem_name = kstrdup(name);
        if (sem->sem_name == NULL) {
                kfree(sem);
                return NULL;
        }

	sem->sem_wchan = wchan_create(sem->sem_name);
	if (sem->sem_wchan == NULL) {
		kfree(sem->sem_name);
		kfree(sem);
		return NULL;
	}

	spinlock_init(&sem->sem_lock);
        sem->sem_count = initial_count;

        return sem;
}

void
sem_destroy(struct semaphore *sem)
{
        KASSERT(sem != NULL);

	/* wchan_cleanup will assert if anyone's waiting on it */
	spinlock_cleanup(&sem->sem_lock);
	wchan_destroy(sem->sem_wchan);
        kfree(sem->sem_name);
        kfree(sem);
}

void 
P(struct semaphore *sem)
{
        KASSERT(sem != NULL);

        /*
         * May not block in an interrupt handler.
         *
         * For robustness, always check, even if we can actually
         * complete the P without blocking.
         */
        KASSERT(curthread->t_in_interrupt == false);

	spinlock_acquire(&sem->sem_lock);
        while (sem->sem_count == 0) {
		/*
		 * Bridge to the wchan lock, so if someone else comes
		 * along in V right this instant the wakeup can't go
		 * through on the wchan until we've finished going to
		 * sleep. Note that wchan_sleep unlocks the wchan.
		 *
		 * Note that we don't maintain strict FIFO ordering of
		 * threads going through the semaphore; that is, we
		 * might "get" it on the first try even if other
		 * threads are waiting. Apparently according to some
		 * textbooks semaphores must for some reason have
		 * strict ordering. Too bad. :-)
		 *
		 * Exercise: how would you implement strict FIFO
		 * ordering?
		 */
		wchan_lock(sem->sem_wchan);
		spinlock_release(&sem->sem_lock);
                wchan_sleep(sem->sem_wchan);

		spinlock_acquire(&sem->sem_lock);
        }
        KASSERT(sem->sem_count > 0);
        sem->sem_count--;
	spinlock_release(&sem->sem_lock);
}

void
V(struct semaphore *sem)
{
        KASSERT(sem != NULL);

	spinlock_acquire(&sem->sem_lock);

        sem->sem_count++;
        KASSERT(sem->sem_count > 0);
	wchan_wakeone(sem->sem_wchan);

	spinlock_release(&sem->sem_lock);
}

////////////////////////////////////////////////////////////
//
// Lock.

struct lock *
lock_create(const char *name)
{
        struct lock *mylock;

        mylock = kmalloc(sizeof(struct lock));
        if (mylock == NULL) {
                return NULL;
        }

        mylock->lk_name = kstrdup(name);
        if (mylock->lk_name == NULL) {
                kfree(mylock);
                return NULL;
        }
        
        // add stuff here as needed

        mylock->lk_wchan = wchan_create(mylock->lk_name);
        if (mylock->lk_wchan == NULL) {
        	kfree(mylock->lk_name);
        	kfree(mylock);
        	return NULL;
        }
        spinlock_init(&mylock->lk_spinlock);
        mylock->owner_thread = NULL;

        //end of added stuff

        return mylock;
}

void
lock_destroy(struct lock *mylock)
{
        KASSERT(mylock != NULL);

        // add stuff here as needed
        
        spinlock_cleanup(&mylock->lk_spinlock);
        wchan_destroy(mylock->lk_wchan);

        //end of added stuff

        kfree(mylock->lk_name);
        kfree(mylock);
}

void
lock_acquire(struct lock *mylock)
{
//	Write this

	if(curthread == mylock->owner_thread) {
		return;
	}
	spinlock_acquire(&mylock->lk_spinlock);
	while (mylock->owner_thread != NULL) {
		wchan_lock(mylock->lk_wchan);
		spinlock_release(&mylock->lk_spinlock);
		wchan_sleep(mylock->lk_wchan);
		spinlock_acquire(&mylock->lk_spinlock);
	}
	mylock->owner_thread = curthread;
	spinlock_release(&mylock->lk_spinlock);

//	end of added stuff

//	(void)mylock;  // suppress warning until code gets written
}

void
lock_release(struct lock *mylock)
{
//	Write this

	if(curthread != mylock->owner_thread) {
		return;
	}
	spinlock_acquire(&mylock->lk_spinlock);
	mylock->owner_thread = NULL;
	wchan_wakeone(mylock->lk_wchan);
	spinlock_release(&mylock->lk_spinlock);

	//end of added stuff

//	(void)mylock;  // suppress warning until code gets written
}

bool
lock_do_i_hold(struct lock *mylock)
{
//	Write this

	return curthread == mylock->owner_thread;

//	end of added stuff

//	(void)mylock;  // suppress warning until code gets written

//	return true; // dummy until code gets written
}

////////////////////////////////////////////////////////////
//
// CV


struct cv *
cv_create(const char *name)
{
        struct cv *mycv;

        mycv = kmalloc(sizeof(struct cv));
        if (mycv == NULL) {
                return NULL;
        }

        mycv->cv_name = kstrdup(name);
        if (mycv->cv_name==NULL) {
                kfree(mycv);
                return NULL;
        }
        
        // add stuff here as needed
        
        spinlock_init(&mycv->cv_spinlock);
        mycv->cv_wchan = wchan_create(mycv->cv_name);
        if (mycv->cv_wchan == NULL) {
        	kfree(mycv->cv_name);
        	kfree(mycv);
        	return NULL;
        }

        //end of added stuff

        return mycv;
}

void
cv_destroy(struct cv *mycv)
{
        KASSERT(mycv != NULL);

        // add stuff here as needed

        wchan_destroy(mycv->cv_wchan);

        //end of added stuff
        kfree(mycv->cv_name);
        kfree(mycv);
}

void
cv_wait(struct cv *mycv, struct lock *mylock)
{
	// Write this

	spinlock_acquire(&mycv->cv_spinlock);
	lock_release(mylock);
	wchan_lock(mycv->cv_wchan);
	spinlock_release(&mycv->cv_spinlock);
	wchan_sleep(mycv->cv_wchan);
	lock_acquire(mylock);

	//end of added stuff
//	(void)mycv;    // suppress warning until code gets written
//	(void)mylock;  // suppress warning until code gets written
}

void
cv_signal(struct cv *mycv, struct lock *mylock)
{
	// Write this

	wchan_wakeone(mycv->cv_wchan);

	//end of added stuff
//	(void)mycv;    // suppress warning until code gets written
	(void)mylock;  // suppress warning until code gets written
}

void
cv_broadcast(struct cv *mycv, struct lock *mylock)
{
	// Write this

	wchan_wakeall(mycv->cv_wchan);

	//end of added stuff
//	(void)mycv;    // suppress warning until code gets written
	(void)mylock;  // suppress warning until code gets written
}
struct rwlock * rwlock_create(const char *name){

	struct rwlock *myrwlock;

	myrwlock=kmalloc(sizeof(struct rwlock));

	if(myrwlock==NULL){
		return NULL;
	}

	myrwlock->rwlock_name = kstrdup(name);

	if(myrwlock->rwlock_name==NULL){
		kfree(myrwlock);
		return NULL;
	}

	myrwlock->lk_read=lock_create("read");
	myrwlock->lk_write=lock_create("write");
	myrwlock->max_readers=30;
	myrwlock->sem_resource=sem_create("resource",myrwlock->max_readers);


	if(myrwlock->sem_resource==NULL){
		kfree(myrwlock);
		return NULL;
	}

	return myrwlock;
}

void rwlock_destroy(struct rwlock *myrwlock){
	lock_destroy(myrwlock->lk_read);
	lock_destroy(myrwlock->lk_write);
	sem_destroy(myrwlock->sem_resource);
	kfree(myrwlock->rwlock_name);
	kfree(myrwlock);
}

void rwlock_acquire_read(struct rwlock *myrwlock){

	lock_acquire(myrwlock->lk_read);

	P(myrwlock->sem_resource);

	lock_release(myrwlock->lk_read);

}

void rwlock_release_read(struct rwlock *myrwlock){

	V(myrwlock->sem_resource);
}

void rwlock_acquire_write(struct rwlock *myrwlock){

	lock_acquire(myrwlock->lk_write);

	for(int i=0;i<myrwlock->max_readers;i++){
		P(myrwlock->sem_resource);
	}

	lock_release(myrwlock->lk_write);

}

void rwlock_release_write(struct rwlock *myrwlock){
	for(int i=0;i<myrwlock->max_readers;i++){
			V(myrwlock->sem_resource);
		}
}
