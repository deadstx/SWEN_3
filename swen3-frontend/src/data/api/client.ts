const API_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export async function request<T>(path: string, options?: RequestInit): Promise<T> {
    const response = await fetch(`${API_URL}${path}`, options);

    if (!response.ok) {
        let message = `Request failed with status ${response.status}`;

        try {
            const problem = (await response.json()) as { detail?: string };
            message = problem.detail ?? message;
        } catch {
            // The backend did not return a JSON problem response.
        }

        throw new Error(message);
    }

    if (response.status === 204) {
        return undefined as T;
    }

    return response.json() as Promise<T>;
}