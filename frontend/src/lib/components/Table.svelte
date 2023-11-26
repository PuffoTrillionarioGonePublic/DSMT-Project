<script lang="ts">
	import { renderElement } from '$lib/utils';

	export let header: string[] = [];
	export let rows: string[][] = [];
</script>

<div class="relative overflow-x-auto w-full">
	<table class="w-full text-sm text-left rtl:text-right text-gray-500">
		<thead class="text-xs text-gray-700 uppercase bg-gray-50">
			<tr>
				{#each header as h}
					<th scope="col" class="px-6 py-3">
						{h}
					</th>
				{/each}
			</tr>
		</thead>
		<tbody>
			{#each rows as row}
				<tr class="bg-white border-b">
					{#each row as el}
						<th scope="row" class="px-6 py-2 font-medium text-gray-900 whitespace-nowrap">
							{renderElement(el)}
						</th>
					{/each}
				</tr>
			{/each}
		</tbody>
	</table>
</div>
