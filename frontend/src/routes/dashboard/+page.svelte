<script lang="ts">
	import Table from '$lib/components/Table.svelte';
	import Script from '$lib/components/Script.svelte';
	import { user } from '$lib/auth';
	import { onMount } from 'svelte';
	import { goto } from '$app/navigation';
	import { statement, get_tables, list_resources, list_user_access, query } from '$lib/requests';
	import Accordion from '$lib/components/Accordion.svelte';
	import Alert from '$lib/components/Alert.svelte';
	import Tab from '$lib/components/Tab.svelte';
	import Prepared from '$lib/components/Prepared.svelte';
	import { renderValue } from '$lib/utils';

	let db = '';
	let table = '';

	let queryProps = {
		query: 'Select * from test;'
	};

	let bindProps = {
		statement: 'Select * from test WHERE id = ?;',
		params: []
	};

	let result = {
		columnNames: [],
		rows: [],
		error: ''
	};

	let databases: any[] = [];
	let error = '';
	let time = '';

	const wrap = async (fn: () => Promise<void>) => {
		time = '';
		error = '';
		result = {
			columnNames: [],
			rows: [],
			error: ''
		};

		if (db == '') {
			error = 'no db selected';
			return;
		}

		const startTime = performance.now();
		try {
			const obj = await fn(); // Execute the passed function
			console.log(obj);

			//@ts-ignore
			if (obj.error) {
				//@ts-ignore
				error = obj.error;
				result = {
					columnNames: [],
					rows: [],
					error: ''
				};
				return;
			} else {
				//@ts-ignore
				result = obj;
			}
		} catch (err) {
			//@ts-ignore
			error = err.response.data.error;
			return;
		}
		const endTime = performance.now();
		const duration = endTime - startTime;
		time = `Execution time: ${duration.toFixed(2)} milliseconds`;
	};

	const executebind = async () => {
		const params = bindProps.params.map((e) => {
			return [parseInt(e[0]), renderValue(e)];
		});

		return await statement({
			params,
			statement: bindProps.statement,
			db: db.split('/')[1],
			bucket: db.split('/')[0]
		});
	};

	const execute = async () => {
		return await query({
			...queryProps,
			db: db.split('/')[1],
			bucket: db.split('/')[0]
		});
	};

	const fetch_table = async () => {
		tab_index = 0;
		queryProps.query = `SELECT * FROM ${table};`;

		return await query({
			...queryProps,
			db: db.split('/')[1],
			bucket: db.split('/')[0]
		});
	};

	onMount(async () => {
		if (!$user) {
			goto('/signin', { replaceState: true });
		}

		try {
			let res: any;

			if (!$user.admin) {
				res = await list_user_access();
			} else {
				res = await list_resources();
			}

			databases = res.files;
		} catch {}
	});

	let tab_index = 0;
</script>

<div class="flex flex-col flex-grow">
	<div class="mb-2 w-full flex items-center justify-end">
		Currently using the database
		<input
			class="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg block p-2.5 disabled:opacity-50 mx-2"
			disabled
			type="text"
			bind:value={db}
		/>
	</div>

	<div class="flex w-full space-x-5 flex-grow">
		<div class="w-1/3 flex-grow">
			<Accordion
				click={() => wrap(fetch_table)}
				bind:selectedGroup={db}
				bind:groups={databases}
				bind:selected={table}
				fetch_group={(db) => get_tables(db)}
			/>
		</div>

		<div class="flex-grow flex flex-col justify-between space-y-5 w-full overflow-hidden">
			<div class="space-y-5">
				<Tab bind:cur={tab_index}>
					<div>
						<svg
							xmlns="http://www.w3.org/2000/svg"
							fill="currentColor"
							class="w-4 h-4 me-2"
							viewBox="0 0 16 16"
						>
							<path
								fill-rule="evenodd"
								d="M8.646 5.646a.5.5 0 0 1 .708 0l2 2a.5.5 0 0 1 0 .708l-2 2a.5.5 0 0 1-.708-.708L10.293 8 8.646 6.354a.5.5 0 0 1 0-.708zm-1.292 0a.5.5 0 0 0-.708 0l-2 2a.5.5 0 0 0 0 .708l2 2a.5.5 0 0 0 .708-.708L5.707 8l1.647-1.646a.5.5 0 0 0 0-.708z"
							/>
							<path
								d="M3 0h10a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2v-1h1v1a1 1 0 0 0 1 1h10a1 1 0 0 0 1-1V2a1 1 0 0 0-1-1H3a1 1 0 0 0-1 1v1H1V2a2 2 0 0 1 2-2z"
							/>
							<path
								d="M1 5v-.5a.5.5 0 0 1 1 0V5h.5a.5.5 0 0 1 0 1h-2a.5.5 0 0 1 0-1H1zm0 3v-.5a.5.5 0 0 1 1 0V8h.5a.5.5 0 0 1 0 1h-2a.5.5 0 0 1 0-1H1zm0 3v-.5a.5.5 0 0 1 1 0v.5h.5a.5.5 0 0 1 0 1h-2a.5.5 0 0 1 0-1H1z"
							/>
						</svg>Script
					</div>

					<div>
						<svg
							class="w-4 h-4 me-2"
							aria-hidden="true"
							xmlns="http://www.w3.org/2000/svg"
							fill="currentColor"
							viewBox="0 0 18 18"
						>
							<path
								d="M6.143 0H1.857A1.857 1.857 0 0 0 0 1.857v4.286C0 7.169.831 8 1.857 8h4.286A1.857 1.857 0 0 0 8 6.143V1.857A1.857 1.857 0 0 0 6.143 0Zm10 0h-4.286A1.857 1.857 0 0 0 10 1.857v4.286C10 7.169 10.831 8 11.857 8h4.286A1.857 1.857 0 0 0 18 6.143V1.857A1.857 1.857 0 0 0 16.143 0Zm-10 10H1.857A1.857 1.857 0 0 0 0 11.857v4.286C0 17.169.831 18 1.857 18h4.286A1.857 1.857 0 0 0 8 16.143v-4.286A1.857 1.857 0 0 0 6.143 10Zm10 0h-4.286A1.857 1.857 0 0 0 10 11.857v4.286c0 1.026.831 1.857 1.857 1.857h4.286A1.857 1.857 0 0 0 18 16.143v-4.286A1.857 1.857 0 0 0 16.143 10Z"
							/>
						</svg>Prepared Stmt
					</div>
				</Tab>

				{#if tab_index == 0}
					<Script bind:query={queryProps.query} execute={() => wrap(execute)} />
				{:else if tab_index == 1}
					<Prepared
						bind:query={bindProps.statement}
						bind:params={bindProps.params}
						execute={() => wrap(executebind)}
					/>
				{:else if tab_index == 2}
					<div>miao</div>
				{/if}
				<!-- if no table is specified show an empty script otherwise show a table-->
			</div>

			{#if error != ''}
				<Alert>{error}</Alert>
			{/if}
			{#if time}
				<div class="w-full relative bg-gray-50 rounded-lg p-3 mb-5">{time}</div>
			{/if}

			<div class="flex flex-col flex-grow w-full relative">
				<div class="h-full w-full relative bg-gray-50 rounded-lg">
					<div class="absolute w-full h-full overflow-auto rounded-lg">
						<Table bind:header={result.columnNames} bind:rows={result.rows} />
					</div>
				</div>
			</div>
		</div>
	</div>
</div>
