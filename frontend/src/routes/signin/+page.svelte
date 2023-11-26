<script lang="ts">
	import { goto } from '$app/navigation';
	import { signin } from '$lib/requests';
	import { user } from '$lib/auth';
	import Alert from '$lib/components/Alert.svelte';
	import { onMount } from 'svelte';

	onMount(() => {
		if ($user) {
			goto('/', { replaceState: true });
		}
	});

	let error = '';
	let props = {
		username: '',
		password: ''
	};

	const _signin = async () => {
		try {
			const res = await signin(props);
			$user = res;
			goto('/dashboard', { replaceState: true });
		} catch (err) {
			//@ts-ignore
			error = err.response.data.error;
			props.password = '';
		}
	};
</script>

<div class="flex w-full justify-center">
	<div class="flex items-center justify-center md:space-x-20">
		<img src="logo.2.png" class="hidden md:block h-72" alt="Flowbite Logo" />
		<div class="h-full border-r-2 border-slate-800 hidden md:flex items-center" />

		<div class="shrink-0 w-full bg-white rounded-lg shadow dark:border md:mt-0 sm:max-w-md xl:p-0">
			<div class="p-6 space-y-4 md:space-y-6 sm:p-8">
				<h1 class="text-xl font-bold leading-tight tracking-tight text-gray-900 md:text-2xl">
					Sign in to your account
				</h1>
				<form on:submit|preventDefault={_signin} class="space-y-4 md:space-y-6" action="#">
					<div>
						<label for="username" class="block mb-2 text-sm font-medium text-gray-900"
							>Your username</label
						>
						<input
							bind:value={props.username}
							type="text"
							name="username"
							id="username"
							class="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-sky-600 focus:border-sky-600 block w-full p-2.5"
							placeholder="kek#123"
							required
						/>
					</div>
					<div>
						<label for="password" class="block mb-2 text-sm font-medium text-gray-900"
							>Password</label
						>
						<input
							bind:value={props.password}
							type="password"
							name="password"
							id="password"
							placeholder="••••••••"
							class="bg-gray-50 border border-gray-300 text-gray-900 sm:text-sm rounded-lg focus:ring-sky-600 focus:border-sky-600 block w-full p-2.5"
							required
						/>
					</div>

					{#if error != ''}
						<Alert>{error}</Alert>
					{/if}

					<button
						type="submit"
						class="w-full text-white bg-sky-600 hover:bg-sky-700 focus:ring-4 focus:outline-none focus:ring-sky-300 font-medium rounded-lg text-sm px-5 py-2.5 text-center"
						>Sign in</button
					>
					<p class="text-sm font-light text-gray-500 dark:text-gray-400">
						Don't have an account yet? <a
							href="/signup"
							class="font-medium text-primary-600 hover:underline dark:text-primary-500">Sign up</a
						>
					</p>
				</form>
			</div>
		</div>
	</div>
</div>
