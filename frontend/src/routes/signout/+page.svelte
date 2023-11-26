<script lang="ts">
	import { goto } from "$app/navigation";
	import { signout } from "$lib/requests";
	import { onMount } from "svelte";
    
    // once the page is loaded go to signout
    onMount(async() => {
        await signout();
        goto("/", {replaceState: true})
    })
</script>
