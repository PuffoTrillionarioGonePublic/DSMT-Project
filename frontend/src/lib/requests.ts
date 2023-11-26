import axios from "axios";
import type { AxiosRequestConfig } from "axios";

import { get as getStoreValue } from 'svelte/store'
import { user } from "./auth";

const auth = (): AxiosRequestConfig | undefined => {
    // get the token from the user store (which is saved at signin in localstorage)
    const token = getStoreValue(user)?.token;
    if (token === undefined || token === null) {
        return;
    }

    // create the AxiosRequestConfig with the provided token
    let headers: AxiosRequestConfig['headers'] = {
        "Content-Type": 'application/json',
        Authorization: `Bearer ${token}`
    };

    return { headers };
};

const BASE_URL = `${window.origin}/api/v1`

export const signin = async (props: any) => {
    const url = `${BASE_URL}/user/login`;
    const { data } = await axios.post(url, props);
    return data;
}

export const signup = async (props: any) => {
    const url = `${BASE_URL}/user/signup`;
    const { data } = await axios.post(url, props, auth());
    return data;
}

export const signout = async () => {
    user.set(null);
}

export const lib_version = async (): Promise<string> => {
    const url = `${BASE_URL}/lib_version`;
    const { data } = await axios.get(url);
    return data;
}

export const set_busy_timeout = async (props: any): Promise<Boolean> => {
    const url = `${BASE_URL}/logged/set_busy_timeout`;
    const { data } = await axios.post(url, props, auth());
    return data;
}

export const get_tables = async (input: string): Promise<any> => {
    const bucket = input.split("/")[0];
    const db = input.split("/")[1];
    const url = `${BASE_URL}/logged/tables?bucket=${encodeURI(bucket)}&db=${encodeURI(db)}`;
    const { data } = await axios.get(url, auth());
    return data;
}


export const columns = async (db: string, table: string): Promise<any> => {
    const url = `${BASE_URL}/columns?db=${encodeURI(db)}&table=${encodeURI(table)}`;
    const { data } = await axios.get(url, auth());
    return data;
}

export const query = async (props: any): Promise<any> => {
    const url = `${BASE_URL}/logged/query`;
    const { data } = await axios.post(url, props, auth());
    return data;
}

export const statement = async (props: any): Promise<any> => {
    const url = `${BASE_URL}/logged/statement`;
    const { data } = await axios.post(url, props, auth());
    return data;
}

export const list_user_access = async (): Promise<any> => {
    const url = `${BASE_URL}/logged/list_user_access?username=${encodeURI(getStoreValue(user)?.username)}`;
    const { data } = await axios.get(url, auth());
    return data;
}

export const list_admin_access = async (): Promise<any> => {
    const url = `${BASE_URL}/admin/list_user_access?username=${encodeURI(getStoreValue(user)?.username)}`;
    const { data } = await axios.get(url, auth());
    return data;
}


export const list_access = async (): Promise<any> => {
    let url = `${BASE_URL}/admin/users`;
    let { data } = await axios.get(url, auth());    

    let map = new Map();
    for(let a of data.users){
        //@ts-ignore
        map[a] = [];
    }

    url = `${BASE_URL}/admin/list_access?sortBy=username`;
    data = (await axios.get(url, auth())).data;
    
    for(let a of data.access){
        //@ts-ignore
        map[a[0]].push(a[1]);
    }

    let res = [];

    for(let a in map){
        res.push([]);

        //@ts-ignore
        res[res.length-1].push(a);
        //@ts-ignore
        if (map[a] !== undefined){
            //@ts-ignore
            res[res.length-1].push(...map[a]);
        }
    }
    
    return res;
}

const list_buckets = async (): Promise<any> => {
    const url = `${BASE_URL}/admin/bucket`;
    const { data } = await axios.get(url, auth());
    return data;
}
const get_bucket = async (bucket: string): Promise<any> => {
    const url = `${BASE_URL}/admin/bucket/${bucket}`;
    const { data } = await axios.get(url, auth());
    return data;
}

export const list_resources = async (): Promise<any> => {
    const obj = await list_buckets();
    let data: string[] = [];
    for (const el of obj.buckets) {
        const bucketObj = await get_bucket(el);
        for (const f of bucketObj.files) {
            data.push(el + "/" + f);
        }
    }
    return {
        files: data
    };
}

export const grant = async (props: any): Promise<any> => {
    const url = `${BASE_URL}/admin/grant`;
    const { data } = await axios.post(url, props, auth());
    return data;
}

export const revoke = async (props: any): Promise<any> => {
    const url = `${BASE_URL}/admin/revoke_access`;
    const { data } = await axios.post(url, props, auth());
    return data;
}
