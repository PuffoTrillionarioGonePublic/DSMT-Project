<script lang="ts">
	import { onMount } from 'svelte';

	export let options: string[] = [];
	export let value: any;
	let showing: string[] = [];

	const filter = async (e: any) => {
		showing = options.filter((x) => x.startsWith(e.currentTarget.value));
	};

	let elem: HTMLInputElement;
	let toggle: any;

	onMount(() => {
		window.onclick = async (e: Event) => {
			if (toggle.contains(e.target)) {
				return;
			}
			await new Promise((r) => setTimeout(r, 100));
			showing = [];
		};

		return () => {
			window.onclick = null;
		};
	});
</script>

<div class="relative my-auto" bind:this={toggle}>
	<input
		bind:this={elem}
		on:input={(e) => {
			filter(e);
		}}
		on:click={(e) => {
			filter(e);
		}}
		bind:value
		type="text"
		id="first_name"
		class="bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5"
		placeholder="John"
		required
	/>

	{#if showing.length != 0}
		<ul
			class="absolute top-10 left-0 w-full text-sm text-left rtl:text-right text-gray-500 p-2 bg-gray-50"
		>
			{#each showing as s}
				<div>
					<button
						type="button"
						on:click={() => {
							value = s;
							elem.focus();
						}}
						class="w-full text-left bg-gray-50 border-b">{s}</button
					>
				</div>
			{/each}
		</ul>
	{/if}
</div>
