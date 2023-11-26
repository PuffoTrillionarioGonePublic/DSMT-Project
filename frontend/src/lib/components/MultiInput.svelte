<script lang="ts">
	import Alert from './Alert.svelte';

	export let input: any[] = [0, null];

	$: input[0],
		() => {
			if (input[0] == 0) {
				input[1] = null;
			}
		};
</script>

<select
	bind:value={input[0]}
	class="max-w-xs bg-gray-50 border border-gray-300 text-gray-900 text-sm rounded-lg focus:ring-blue-500 focus:border-blue-500 block w-full p-2.5"
>
	<option value="0">Null</option>
	<option value="1">Integer</option>
	<option value="2">Float</option>
	<option value="3">String</option>
	<option value="4">Blob</option>
</select>

{#if input[0] == 0}
	<input
		on:loadstart={() => (input[1] = null)}
		bind:value={input[1]}
		disabled
		class="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-sky-600 focus:border-sky-600 block w-full p-2.5 disabled:opacity-25"
	/>
{:else if input[0] == 1}
	<input
		type="number"
		step="1"
		bind:value={input[1]}
		class="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-sky-600 focus:border-sky-600 block w-full p-2.5"
	/>
{:else if input[0] == 2}
	<input
		type="number"
		bind:value={input[1]}
		class="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-sky-600 focus:border-sky-600 block w-full p-2.5"
	/>
{:else if input[0] == 3}
	<input
		type="text"
		bind:value={input[1]}
		class="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-sky-600 focus:border-sky-600 block w-full p-2.5"
	/>
{:else if input[0] == 4}
	<input
		type="text"
		bind:value={input[1]}
		class="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-sky-600 focus:border-sky-600 block w-full p-2.5"
	/>
{:else}
	<Alert>Wrong type</Alert>
{/if}
