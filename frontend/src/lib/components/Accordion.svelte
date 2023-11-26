<script lang="ts">
	import { onMount } from 'svelte';

	export let groups: string[] = [];
	let openings: Boolean[] = [];
	let groupsContent: string[][] = [];

	export let selectedGroup = '';
	export let selected = '';

	onMount(() => {
		groups.forEach(() => {
			openings = [...openings, false];
			groupsContent = [...groupsContent, []];
		});
	});

	const fetch = async (i: number) => {
		try {
			if (!openings[i]) {
				const obj = await fetch_group(groups[i]);
				groupsContent[i] = obj.tables;
			}
		} catch {
			groupsContent[i] = [];
		}
		openings[i] = !openings[i];
	};

	export let fetch_group = async (group: string): Promise<any> => {};
	export let click = async (): Promise<any> => {};
</script>

<div class="bg-white rounded-md shadow h-full w-full overflow-hidden">
	{#each groups as group, i}
		<div>
			<div class="flex items-center py-2 px-4">
				<button
					class="p-1 cursor-pointer text-gray-900 hover:bg-gray-200 rounded-lg"
					on:click={() => {
						fetch(i);
					}}
				>
					<svg
						xmlns="http://www.w3.org/2000/svg"
						fill="none"
						viewBox="0 0 24 24"
						stroke-width="1.5"
						stroke="currentColor"
						class="w-4 h-4"
					>
						{#if openings[i]}
							<path stroke-linecap="round" stroke-linejoin="round" d="M18 12H6" />
						{:else}
							<path stroke-linecap="round" stroke-linejoin="round" d="M12 6v12m6-6H6" />
						{/if}
					</svg>
				</button>

				<button
					class="flex"
					on:click={() => {
						selectedGroup = group;
						fetch(i);
					}}
				>
					<span
						class={selectedGroup == group
							? 'mx-1 font-medium underline underline-offset-1'
							: 'mx-1'}>{group}</span
					>
					<svg
						xmlns="http://www.w3.org/2000/svg"
						fill="none"
						viewBox="0 0 24 24"
						stroke-width="1.5"
						stroke="currentColor"
						class="w-4 h-4 mt-1"
					>
						<path
							stroke-linecap="round"
							stroke-linejoin="round"
							d="M20.25 6.375c0 2.278-3.694 4.125-8.25 4.125S3.75 8.653 3.75 6.375m16.5 0c0-2.278-3.694-4.125-8.25-4.125S3.75 4.097 3.75 6.375m16.5 0v11.25c0 2.278-3.694 4.125-8.25 4.125s-8.25-1.847-8.25-4.125V6.375m16.5 0v3.75m-16.5-3.75v3.75m16.5 0v3.75C20.25 16.153 16.556 18 12 18s-8.25-1.847-8.25-4.125v-3.75m16.5 0c0 2.278-3.694 4.125-8.25 4.125s-8.25-1.847-8.25-4.125"
						/>
					</svg>
				</button>
			</div>
			{#if openings[i]}
				{#each groupsContent[i] as el}
					<div>
						<button
							class="text-sm text-gray-600 py-1 px-8"
							on:click={async () => {
								selectedGroup = group;
								selected = el;
								await click();
							}}
						>
							{el}
						</button>
					</div>
				{:else}
					<div />
				{/each}
			{/if}
		</div>
	{/each}
</div>
