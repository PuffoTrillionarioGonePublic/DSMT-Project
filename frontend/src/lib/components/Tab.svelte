<script lang="ts">
	import { onMount } from "svelte";

    export let cur : number;
        
     
    let tabs : any= [];    
    let tabel : HTMLDivElement;

    onMount(() => {
        tabs = [];
        
        [...tabel.children].forEach(el  => {
            tabs = [...tabs, el.innerHTML]
        })

    })    
</script>

<div bind:this={tabel} class="hidden">
    <slot></slot>
</div>

<div class="border-b border-gray-400">
    <ul class="flex flex-wrap -mb-px text-sm font-medium text-center text-gray-600">
        {#each tabs as tab, i}
            <li class="me-2">
                <button on:click={() => cur = i}>
                    {#if i == cur}
                        <div class="inline-flex items-center justify-center py-2 px-4 text-blue-600 border-b-2 border-blue-600 rounded-t-lg active group" aria-current="page">
                            {@html tab}
                        </div>
                    {:else}
                        <div class="inline-flex items-center justify-center py-2 px-4 border-b-2 border-transparent rounded-t-lg hover:text-gray-600 hover:border-gray-300 group">
                            {@html tab}
                        </div>
                    {/if}
                </button>
            </li>
        {/each}
    </ul>
</div>
