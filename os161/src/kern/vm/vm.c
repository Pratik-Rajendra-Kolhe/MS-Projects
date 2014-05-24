/*
 *vm.c
 *Added by:Pratik
 * Created on: May 2, 2014
 *      Author: trinity
 */

#include <types.h>
#include <kern/errno.h>
#include <lib.h>
#include <spl.h>
#include <spinlock.h>
#include <thread.h>
#include <current.h>
#include <mips/tlb.h>
#include <addrspace.h>
#include <vm.h>
#include <coremap.h>
#include <mainbus.h>


struct spinlock coremap_slock = SPINLOCK_INITIALIZER;

static paddr_t firstpaddr;

void
vm_bootstrap(void)
{
    //coremap bootstrap
    firstpaddr=coremap_bootstrap();
    //lastpaddr=mainbus_ramsize();

}


vaddr_t
alloc_kpages(int npages){

    paddr_t paddr;
    spinlock_acquire(&coremap_slock);
    paddr=getpaddr(npages);
    if(paddr==0 || npages == 0){
    	spinlock_release(&coremap_slock);
        return 0;
    }
    addtocoremap(paddr,npages,1);
    spinlock_release(&coremap_slock);
    return PADDR_TO_KVADDR(paddr);
}

int
page_alloc(int npages, vaddr_t vaddr){
	paddr_t paddr;
	for(int i=0;i<npages;i++){
		spinlock_acquire(&coremap_slock);
		paddr=getpaddr(1);
		if(paddr==0){
			spinlock_release(&coremap_slock);
			return 1;
		}
		addtocoremap(paddr,1,0);
		spinlock_release(&coremap_slock);
		addPageEntry(vaddr,paddr, 1, 1, 0);
		curthread->t_addrspace->heapNPages++;
		vaddr += PAGE_SIZE;
	}
	return 0;
}



void
page_free(paddr_t paddr)
{
    spinlock_acquire(&coremap_slock);
    removefromcoremap(paddr);
    spinlock_release(&coremap_slock);
}

void
free_kpages(vaddr_t vaddr)
{
    spinlock_acquire(&coremap_slock);
    removefromcoremap(KVADDR_TO_PADDR(vaddr));
    spinlock_release(&coremap_slock);
}

int
sys_sbrk(intptr_t amount,int *retval){

    vaddr_t vaddr = curthread->t_addrspace->vbreaktop;
    curthread->t_addrspace->vbreaktop += amount;

    if(curthread->t_addrspace->vbreaktop >= (STACK_BASE - 4096)) {
    	return ENOMEM;
    }

    if(curthread->t_addrspace->vbreaktop < curthread->t_addrspace->vbreakbase) {
    	curthread->t_addrspace->vbreaktop = vaddr;
    	return EINVAL;
    }

    if((curthread->t_addrspace->vbreaktop - curthread->t_addrspace->vbreakbase) >= (curthread->t_addrspace->heapNPages * PAGE_SIZE)) {
        int npages = curthread->t_addrspace->vbreaktop - (curthread->t_addrspace->vbreakbase + (curthread->t_addrspace->heapNPages * PAGE_SIZE));
        int mod=npages % PAGE_SIZE;
        if(mod == 0){
            npages = npages / PAGE_SIZE;
        }else{
            npages = (npages / PAGE_SIZE) + 1;
        }

        vaddr_t temp;
        if((vaddr % PAGE_SIZE) == 0) {
        	temp = vaddr;
        } else {
        	temp =  (vaddr & PAGE_FRAME) + PAGE_SIZE;
        }
    	if(page_alloc(npages, temp)){
    		return ENOMEM;
    	}
    }

    *retval = vaddr;
    return 0;
}
