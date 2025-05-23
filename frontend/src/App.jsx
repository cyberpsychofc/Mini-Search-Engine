import { useState, useEffect } from 'react';
import axios from 'axios';
import './App.css';

function App() {
  const [query, setQuery] = useState('');
  const [results, setResults] = useState([]);
  const [suggestions, setSuggestions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [hasSearched, setHasSearched] = useState(false);

  // Fetch autocomplete suggestions
  useEffect(() => {
    if (query.trim() === '') {
      setSuggestions([]);
      return;
    }

    const fetchSuggestions = async () => {
      try {
        const response = await axios.get(
          `http://localhost:8080/api/autocomplete?q=${encodeURIComponent(query)}`
        );
        setSuggestions(response.data);
      } catch (err) {
        console.error('Autocomplete error:', err);
        setSuggestions([]);
      }
    };

    const debounceId = setTimeout(fetchSuggestions, 300);
    return () => clearTimeout(debounceId);
  }, [query]);

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!query.trim()) return;

    setLoading(true);
    setError(null);
    setHasSearched(true);
    setSuggestions([]);

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
        setError('No server available to handle your request');
      } else {
        setError(err.message);
      }
      setResults([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSuggestionClick = (suggestion) => {
    setQuery(suggestion);
    setSuggestions([]);
  };

  return (
    <div className="container pt-25 pl-6 pr-6 md:pr-0 md:pl-0 md:pt-18 dark:bg-gray-900 dark:text-white bg-gray-100 text-gray-900">
      <h1 className="text-4xl transition-shadow duration-100 drop-shadow-[0_0_1px_#61afef] md:text-5xl py-4 md:py-10 font-bold font-sans bg-gradient-to-r from-blue-600 via-green-500 to-indigo-400 inline-block text-transparent bg-clip-text">
        Mini Search Engine
      </h1>

      <form onSubmit={handleSearch} className="search-form relative">
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search Anything"
          className="search-input w-full"
        />
        {suggestions.length > 0 && (
          <ul className="absolute z-10 translate-y-[40px] w-[280px] md:w-[735px] bg-white dark:bg-gray-500 mt-1 border border-gray-200 dark:border-gray-700 rounded-md max-h-60 overflow-auto">
            {suggestions.map((suggestion, index) => (
              <li
                key={index}
                onClick={() => handleSuggestionClick(suggestion)}
                className="px-4 py-2 hover:bg-gray-100 dark:hover:bg-gray-700 cursor-pointer"
              >
                {suggestion}
              </li>
            ))}
          </ul>
        )}

        <button
          type="submit"
          className="search-button"
          disabled={loading || !query.trim()}
        >
          {loading ? '◌' : '→'}
        </button>
      </form>

      {error && <p className="error mt-4">{error}</p>}

      <div className="results mt-6">
        {results.length > 0 ? (
          <ul>
            {results.map((result, index) => (
              <li key={index} className="result-item mb-4">
                <a
                  href={result.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-blue-600 dark:text-blue-400 underline"
                >
                  {result.url}
                </a>
                <p>Match Score: {result.score.toFixed(2)}</p>
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