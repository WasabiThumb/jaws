

const nativeObjectValues: boolean = (typeof Object === "function" && typeof Object["values"] === "function");
export function objectValues<V>(v: { [k: string | number | symbol]: V }): V[] {
    if (nativeObjectValues) {
        return (Object as { values: (i: typeof v) => V[] }).values(v);
    } else {
        const keys: string[] = Object.keys(v);
        const values: V[] = new Array(keys.length);
        for (let i=0; i < keys.length; i++) {
            values[i] = (v as unknown as { [k: string]: V })[keys[i]];
        }
        return values;
    }
}
