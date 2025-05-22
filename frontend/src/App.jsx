import { useState } from 'react';
import './App.css';

function App() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [hasSearched, setHasSearched] = useState(false);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!query.trim()) return;

    setLoading(true);
    setError(null);
    setHasSearched(true);

    try {
      const response = await fetch(
        `http://localhost:8080/api/search?q=${encodeURIComponent(query)}`
      );
      if (!response.ok) {
        throw new Error('Search request failed');
      }
      const data = await response.json();
      setResults(data);
    } catch (err) {
      if (err.name === 'TypeError') {
        setError('No server available to serve your request');
      } else {
        setError(err.message);
      }
      setResults([]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container pt-25 pl-6 pr-6 text-white md:pr-0 md:pl-0 md:pt-18 dark:bg-gray-900 dark:text-white bg-gray-100 text-gray-900">
      <h1 className="text-4xl md:text-5xl py-4 md:py-10 font-bold font-sans bg-gradient-to-r from-blue-600 via-green-500 to-indigo-400 inline-block text-transparent bg-clip-text">
        Mini Search Engine
      </h1>

      <form onSubmit={handleSearch} className="search-form">
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Enter your search query"
          className="search-input"
        />
        <button type="submit" className="search-button" disabled={loading}>
          {loading ? 'Searching...' : 'Search'}
        </button>
      </form>

      {error && <p className="error">{error}</p>}

      <div className="results">
        {results.length > 0 ? (
          <ul>
            {results.map((result, index) => (
              <li key={index} className="result-item">
                <a href={result.url} target="_blank" rel="noopener noreferrer">
                  {result.url}
                </a>
                <p>Score: {result.score.toFixed(2)}</p>
              </li>
            ))}
          </ul>
        ) : (
          !loading && !error && hasSearched && <p>No results found.</p>
        )}
      </div>
    </div>
  );
}

export default App;
