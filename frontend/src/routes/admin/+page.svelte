<script lang="ts">
	import { user } from '$lib/auth';
	import { onMount } from 'svelte';
	import Error from '../+error.svelte';
	import { list_resources, list_access, grant, revoke, query } from '$lib/requests';
	import Alert from '$lib/components/Alert.svelte';
	import Selector from '$lib/components/Selector.svelte';

	let accesses: any[] = [];
	let error = '';
	let resources: string[] = [];

	let adding_idx = -1;

	const refresh = async () => {
		accesses = await list_access();

		const obj2 = await list_resources();
		resources = obj2.files;
	};

	onMount(async () => {
		try {
			await refresh();
		} catch (err) {
			//@ts-ignore
			error = err.response.data.error;
		}
	});

	let new_db = '';

	const remove_to_db = async (user: string, db: string) => {
		try {
			await revoke({
				usernames: [user],
				files: [db]
			});
			await refresh();
			adding_idx = -1;
		} catch (err) {
			//@ts-ignore
			error = err.response.data.error;
		}
	};

	const add_to_db = async (user: string) => {
		try {
			await grant({
				usernames: [user],
				files: [new_db]
			});
			adding_idx = -1;

			await query({
				bucket: new_db.split('/')[0],
				db: new_db.split('/')[1],
				query: 'SELECT 1;'
			});

			await refresh();
			new_db = '';
		} catch (err) {
			//@ts-ignore
			error = err.response.data.error;
		}
	};
</script>

{#if !$user || !$user.admin}
	<Error />
{:else}
	<h2 class="text-4xl font-extrabold my-10">Resources</h2>

	<div class="relative overflow-x-auto w-full rounded-lg">
		<table class="w-full text-sm text-left rtl:text-right text-gray-500 bg-gray-50">
			<thead class="text-xs text-gray-700 uppercase bg-gray-50">
				<tr>
					<th scope="col" class="px-6 py-3"> Namespace </th>
					<th scope="col" class="px-6 py-3"> Database </th>
				</tr>
			</thead>
			<tbody>
				{#each resources as obj}
					<tr class="bg-white border-b">
						<th scope="row" class="px-6 py-2 font-medium text-gray-900 whitespace-nowrap">
							{obj.split('/')[0]}
						</th>
						<td class="px-6 py-2 font-medium text-gray-900 whitespace-nowrap">
							{obj.split('/')[1]}
						</td>
					</tr>
				{/each}
			</tbody>
		</table>
	</div>

	<h2 class="text-4xl font-extrabold my-10">Permissions</h2>
	{#if error != ''}
		<div class="mb-2 w-full">
			<Alert>{error}</Alert>
		</div>
	{/if}
	<div class="relative overflow-x-auto w-full rounded-lg">
		<table class="w-full text-sm text-left rtl:text-right text-gray-500">
			<thead class="text-xs text-gray-700 uppercase bg-gray-50">
				<tr>
					<th scope="col" class="px-6 py-3"> Username </th>
					<th scope="col" class="px-6 py-3"> Edit Permissions </th>
				</tr>
			</thead>
			<tbody>
				{#each accesses as access, user_idx}
					<tr class="bg-white border-b">
						<th scope="row" class="px-6 py-2 font-medium text-gray-900 whitespace-nowrap">
							{access[0]}
						</th>
						<th scope="row" class="px-6 py-2 font-medium text-gray-900 whitespace-nowrap">
							<div class="flex flex-wrap flex-grow-0 space-x-2 items-center justify-start">
								{#each access.slice(1) as resource}
									<button
										on:click={() => remove_to_db(access[0], resource)}
										type="button"
										class="text-gray-900 bg-white hover:bg-gray-100 border border-gray-200 focus:ring-4 focus:outline-none focus:ring-gray-100 font-medium rounded-lg text-sm px-5 py-2.5 text-center inline-flex items-center my-1"
									>
										{resource}
										<svg
											xmlns="http://www.w3.org/2000/svg"
											fill="currentColor"
											class="w-3 h-3 ml-1"
											viewBox="0 0 16 16"
										>
											<path
												d="M2.146 2.854a.5.5 0 1 1 .708-.708L8 7.293l5.146-5.147a.5.5 0 0 1 .708.708L8.707 8l5.147 5.146a.5.5 0 0 1-.708.708L8 8.707l-5.146 5.147a.5.5 0 0 1-.708-.708L7.293 8 2.146 2.854Z"
											/>
										</svg>
									</button>
								{/each}
								{#if adding_idx == user_idx}
									<form
										class="flex justify-center space-x-1"
										on:submit|preventDefault={() => {
											add_to_db(access[0]);
										}}
									>
										<Selector bind:value={new_db} bind:options={resources} />

										<button
											type="submit"
											class="text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center inline-flex items-center my-1"
										>
											Add
											<svg
												class="rtl:rotate-180 w-3.5 h-3.5 ms-2"
												aria-hidden="true"
												xmlns="http://www.w3.org/2000/svg"
												fill="none"
												viewBox="0 0 14 10"
											>
												<path
													stroke="currentColor"
													stroke-linecap="round"
													stroke-linejoin="round"
													stroke-width="2"
													d="M1 5h12m0 0L9 1m4 4L9 9"
												/>
											</svg>
										</button>
										<button
											on:click={() => (adding_idx = -1)}
											type="button"
											class="my-auto flex items-center jusitify-center p-3 text-sm font-medium text-gray-900 focus:outline-none bg-white rounded-full border border-gray-200 hover:bg-gray-100 hover:text-blue-700 focus:z-10 focus:ring-4 focus:ring-gray-200"
										>
											<svg
												xmlns="http://www.w3.org/2000/svg"
												fill="currentColor"
												class="w-3 h-3 m-auto"
												viewBox="0 0 16 16"
											>
												<path
													d="M2.146 2.854a.5.5 0 1 1 .708-.708L8 7.293l5.146-5.147a.5.5 0 0 1 .708.708L8.707 8l5.147 5.146a.5.5 0 0 1-.708.708L8 8.707l-5.146 5.147a.5.5 0 0 1-.708-.708L7.293 8 2.146 2.854Z"
												/>
											</svg>
										</button>
									</form>
								{:else}
									<button
										on:click={async () => {
											adding_idx = user_idx;
											const obj = await list_resources();
											resources = obj.files;
										}}
										type="button"
										class="text-white bg-blue-700 hover:bg-blue-800 focus:ring-4 focus:outline-none focus:ring-blue-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center inline-flex items-center my-1"
									>
										Add a database
										<svg
											class="rtl:rotate-180 w-3.5 h-3.5 ms-2"
											aria-hidden="true"
											xmlns="http://www.w3.org/2000/svg"
											fill="none"
											viewBox="0 0 14 10"
										>
											<path
												stroke="currentColor"
												stroke-linecap="round"
												stroke-linejoin="round"
												stroke-width="2"
												d="M1 5h12m0 0L9 1m4 4L9 9"
											/>
										</svg>
									</button>
								{/if}
							</div>
						</th>
					</tr>
				{/each}
			</tbody>
		</table>
	</div>
{/if}
