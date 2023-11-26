<script lang="ts">
	import { page } from '$app/stores';
	let currentPage = "";
	$ : currentPage = $page.url.pathname;

	export let name : string;
	export let route : string;
</script>

{#if route == currentPage}
    <a href="{route}" class="block py-2 px-3 text-white bg-sky-700 rounded md:bg-transparent md:text-sky-700 md:p-0" aria-current="page">{name}</a>
{:else}
    <a href="{route}" class="block py-2 px-3 text-gray-900 rounded hover:bg-gray-100 md:hover:bg-transparent md:border-0 md:hover:text-sky-700 md:p-0">{name}</a>
{/if}
