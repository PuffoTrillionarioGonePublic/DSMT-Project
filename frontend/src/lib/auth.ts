import { writable } from "svelte/store";

const asUser = (user: any): any | null => {
	if (
		user &&
		typeof user.token === "string" &&
		typeof user.username === "string" &&
		typeof user.expirationDate === "string" &&
		typeof user.admin === "boolean"
	) {
		return user;
	} else {
		return null;
	}
}

// Initialize the user store with validation
const userStore = (() => {
	if (!("user" in localStorage)) return null;

	try {
		const storedUser = JSON.parse(localStorage.getItem("user") || "");
		return asUser(storedUser);
	} catch {
		return null;
	}
})();

// export the store as a writable 
export const user = writable<any | null>(userStore);

// write the store to the localstorage time the user changes
user.subscribe((value) => {
	// Only save to localStorage if the value is a valid user object
	if (value) {
		localStorage.user = JSON.stringify(value);
	} else {
		// Clear localStorage if the value is null
		localStorage.removeItem("user");
	}
});
