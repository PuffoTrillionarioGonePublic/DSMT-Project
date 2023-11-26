import { sveltekit } from '@sveltejs/kit/vite';
import { defineConfig } from 'vite';

export default defineConfig({
    server : {
        proxy : {
            '/api' : process.env.BACKEND_API_URL
        },
    },
    plugins: [sveltekit()]
});