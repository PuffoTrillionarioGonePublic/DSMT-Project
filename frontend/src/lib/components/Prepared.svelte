<script lang="ts">
	import MultiInput from './MultiInput.svelte';
	import Script from './Script.svelte';

	export let query = '';
	export let params: any[] = [];

	export let execute = async () => {};
</script>

<Script bind:query {execute} />

<form on:submit|preventDefault={execute}>
	{#each params as input, i}
		<div class="flex space-x-2 mb-2 w-full justify-center items-center">
			<MultiInput {input} />

			<button
				class=" text-white bg-slate-600 hover:bg-slate-700 focus:ring-4 focus:outline-none focus:ring-slate-300 font-medium rounded-full text-sm h-7 w-7 p-2 flex justify-center items-center text-center"
				on:click={() => {
					params.splice(i, 1);
					params = params;
				}}
			>
				<svg
					xmlns="http://www.w3.org/2000/svg"
					fill="currentColor"
					class="w-4 h-4"
					viewBox="0 0 16 16"
				>
					<path
						d="M6.5 1h3a.5.5 0 0 1 .5.5v1H6v-1a.5.5 0 0 1 .5-.5ZM11 2.5v-1A1.5 1.5 0 0 0 9.5 0h-3A1.5 1.5 0 0 0 5 1.5v1H2.506a.58.58 0 0 0-.01 0H1.5a.5.5 0 0 0 0 1h.538l.853 10.66A2 2 0 0 0 4.885 16h6.23a2 2 0 0 0 1.994-1.84l.853-10.66h.538a.5.5 0 0 0 0-1h-.995a.59.59 0 0 0-.01 0H11Zm1.958 1-.846 10.58a1 1 0 0 1-.997.92h-6.23a1 1 0 0 1-.997-.92L3.042 3.5h9.916Zm-7.487 1a.5.5 0 0 1 .528.47l.5 8.5a.5.5 0 0 1-.998.06L5 5.03a.5.5 0 0 1 .47-.53Zm5.058 0a.5.5 0 0 1 .47.53l-.5 8.5a.5.5 0 1 1-.998-.06l.5-8.5a.5.5 0 0 1 .528-.47ZM8 4.5a.5.5 0 0 1 .5.5v8.5a.5.5 0 0 1-1 0V5a.5.5 0 0 1 .5-.5Z"
					/>
				</svg>
			</button>
		</div>
	{/each}

	<div class="flex items-center justify-center">
		<button
			type="button"
			class="text-white bg-sky-600 hover:bg-sky-700 focus:ring-4 focus:outline-none focus:ring-sky-300 font-medium rounded-lg text-sm py-2 px-1.5 text-center"
			on:click={() => (params = [...params, [0, null]])}
		>
			Add a new parameter
		</button>
	</div>
</form>
