<script lang="ts">
	import { onMount } from 'svelte';
	import '../app.css';
	import NavLink from '$lib/components/NavLink.svelte';
	import { user } from '$lib/auth';

	let shonav = false;
	let toggle: any;

	onMount(() => {
		window.onclick = (e: Event) => {
			if (toggle.contains(e.target)) {
				shonav = !shonav;
				return;
			}
			shonav = false;
		};

		return () => (window.onclick = null);
	});
</script>

<div class="active w-full bg-zinc-200 min-h-screen flex flex-col justify-between">
	<nav class="rounded-lg shadow m-4 px-3 bg-white border-gray-200">
		<div class="max-w-screen-xl flex flex-wrap items-center justify-between mx-auto p-4">
			<a href="/" class="flex items-center space-x-3 rtl:space-x-reverse">
				<img src="logo.2.png" class="h-8" alt="Flowbite Logo" />
				<span class="self-center text-2xl font-semibold whitespace-nowrap">ErlDB</span>
			</a>
			<button
				bind:this={toggle}
				data-collapse-toggle="navbar-default"
				type="button"
				class="inline-flex items-center p-2 w-10 h-10 justify-center text-sm text-gray-500 rounded-lg md:hidden hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-gray-200"
				aria-controls="navbar-default"
				aria-expanded="false"
			>
				<span class="sr-only">Open main menu</span>
				<svg
					class="w-5 h-5"
					aria-hidden="true"
					xmlns="http://www.w3.org/2000/svg"
					fill="none"
					viewBox="0 0 17 14"
				>
					<path
						stroke="currentColor"
						stroke-linecap="round"
						stroke-linejoin="round"
						stroke-width="2"
						d="M1 1h15M1 7h15M1 13h15"
					/>
				</svg>
			</button>
			<div class:hidden={!shonav} class="w-full md:block md:w-auto" id="navbar-default">
				<ul
					class="font-medium flex flex-col p-4 md:p-0 mt-4 border border-gray-100 rounded-lg bg-gray-50 md:flex-row md:space-x-8 rtl:space-x-reverse md:mt-0 md:border-0 md:bg-white"
				>
					{#if $user}
						{#if $user.admin}
							<li><NavLink route={'/admin'} name="Admin" /></li>
						{/if}
						<li><NavLink route={'/dashboard'} name="Dashboard" /></li>
						<li><NavLink route={'/signout'} name="Logout" /></li>
					{:else}
						<li><NavLink route={'/signup'} name="Sign up" /></li>
						<li><NavLink route={'/signin'} name="Login" /></li>
					{/if}
				</ul>
			</div>
		</div>
	</nav>

	<div class="container mx-auto p-4 flex flex-col flex-grow justify-center">
		<slot />
	</div>

	<footer class="bg-white rounded-lg shadow m-4">
		<div class="w-full mx-auto max-w-screen-xl p-4 md:flex md:items-center md:justify-between">
			<span class="text-sm text-gray-500 sm:text-center"
				>© 2023 <a href="https://github.com/PuffoTrillionario" class="hover:underline"
					>PuffoTrillonario™</a
				>. All Rights Reserved.
			</span>
		</div>
	</footer>
</div>
