
export function withTimeout<T>(promise: PromiseLike<T>, timeout: number = 5000): Promise<T> {
    return new Promise<T>((res, rej) => {
        let timedOut = false;
        let to = setTimeout(() => {
            if (timedOut) return;
            timedOut = true;
            rej(new Error("Timed out (" + timeout + "ms)"));
        }, timeout);

        promise.then((v: T) => {
            if (timedOut) return;
            clearTimeout(to);
            res(v);
        }, (err) => {
            if (timedOut) return;
            clearTimeout(to);
            rej(err);
        });
    });
}
