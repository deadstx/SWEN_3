const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

async function throwIfNotOk(response: Response): Promise<void> {
    if (response.ok) return;

    let message = `Request failed with status ${response.status}`;

    try {
        const problem = (await response.json()) as { detail?: string };
        message = problem.detail ?? message;
    } catch {
        // The backend did not return a JSON problem response.
    }

    throw new Error(message);
}

export async function request<T>(path: string, options?: RequestInit): Promise<T> {
    const response = await fetch(`${API_URL}${path}`, options);
    await throwIfNotOk(response);

    if (response.status === 204) {
        return undefined as T;
    }

    return response.json() as Promise<T>;
}

export async function requestBlob(path: string, options?: RequestInit): Promise<Blob> {
    const response = await fetch(`${API_URL}${path}`, options);
    await throwIfNotOk(response);

    return response.blob();
}