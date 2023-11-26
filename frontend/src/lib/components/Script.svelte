<script lang="ts">
	export let query = '';
	export let execute = async () => {};

	// Handle keydown event for the textarea
	const scriptKeyDown = (e: KeyboardEvent | undefined) => {
		if (!e) return;
		// Check for Ctrl+Enter (keyCode 13 is Enter)
		if (e.ctrlKey && e.key === 'Enter') {
			execute();
		}
	};
</script>

<div class="w-full mb-4 border border-gray-200 rounded-lg bg-gray-50">
	<form on:submit|preventDefault={execute}>
		<div class="px-4 py-2 bg-white rounded-t-lg">
			<label for="comment" class="sr-only">Your comment</label>
			<textarea
				bind:value={query}
				on:keydown={scriptKeyDown}
				id="comment"
				rows="4"
				class="font-mono font-medium w-full px-0 text-sm text-gray-900 bg-white border-0 !outline-none"
				placeholder="Write a comment..."
				required
			/>
		</div>
		<div class="flex items-center justify-between px-3 py-2 border-t">
			<button
				type="submit"
				class="inline-flex items-center py-2.5 px-4 text-xs font-medium text-center text-white bg-sky-700 rounded-lg focus:ring-sky-200 dark:focus:ring-sky-900 hover:bg-sky-800"
			>
				Execute
			</button>
		</div>
	</form>
</div>
