const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '';

export async function fetchApi<T>(endpoint: string, options: RequestInit = {}): Promise<T> {
  const url = endpoint.startsWith('http') 
    ? endpoint 
    : `${API_BASE_URL}${endpoint.startsWith('/') ? endpoint : `/${endpoint}`}`;

  const defaultHeaders: HeadersInit = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
  };

  const response = await fetch(url, {
    ...options,
    credentials: 'include', // Ensure HttpOnly cookies are automatically sent with requests
    headers: {
      ...defaultHeaders,
      ...options.headers,
    },
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || `API error: ${response.status} ${response.statusText}`);
  }

  // Handle empty responses (204 No Content, 202 Accepted, or empty response body)
  if (response.status === 204 || response.status === 202) {
    return {} as T;
  }

  const text = await response.text();
  if (!text || text.trim().length === 0) {
    return {} as T;
  }

  return JSON.parse(text);
}

export { API_BASE_URL };
