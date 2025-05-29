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
  const [currentPage, setCurrentPage] = useState(1);


  const crawlOnce = (() => {
    let hasCrawled = false;

    return async () => {
      if (hasCrawled) return;
      hasCrawled = true;

      try {
        await axios.get(
          'https://mini-search-engine-0595.onrender.com/crawl?seedUrl=https://open.spotify.com'
        );
        console.log('Crawl initiated successfully.');
      } catch (err) {
        console.error('Crawl error:', err);
      }
    };
  })();

  // Check if the backend server is online
  useEffect(() => {
    const checkPulse = async () => {
      try {
        await axios.get('https://mini-search-engine-r47g.onrender.com/ping');
      } catch (err) {
        setError('No server available to handle your request');
      }
    }

    checkPulse();
  }, []);

  // Fetch autocomplete suggestions
  useEffect(() => {
    if (query.trim() === '') {
      setSuggestions([]);
      return;
    }

    const fetchSuggestions = async () => {
      try {
        const response = await axios.get(
          `https://mini-search-engine-r47g.onrender.com/api/autocomplete?q=${encodeURIComponent(query)}`,
          { withCredentials: true }
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
        `https://mini-search-engine-r47g.onrender.com/api/search?q=${encodeURIComponent(query)}`
      );
      if (!response.ok) {
        throw new Error('Search request failed');
      }
      const data = await response.json();
      setResults(data);
      setCurrentPage(1); // Reset to page 1
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

  const totalPages = Math.ceil(results.length / 10);
  const startIndex = (currentPage - 1) * 10;
  const endIndex = startIndex + 10;
  const currentResults = results.slice(startIndex, endIndex);

  return (
    <div className="container pt-25 pl-6 pr-1 md:pr-0 md:pl-0 md:pt-18 dark:bg-gray-900 dark:text-white bg-gray-100 text-gray-900 flex flex-col min-h-screen">
      <a href="https://github.com/cyberpsychofc/Mini-Search-Engine"
    class="absolute top-0 right-0 m-4 mr-5 w-10 h-10 bg-primary/10 border border-blue-900 rounded text-primary flex items-center justify-center hover:bg-primary/20 transition-colors duration-200 group">
  <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" fill="currentColor"
       class="text-blue-500 group-hover:scale-110 transition-transform duration-200"
       viewBox="0 0 16 16">
    <path
      d="M8 0C3.58 0 0 3.58 0 8c0 3.54 2.29 6.53 5.47 7.59.4.07.55-.17.55-.38 0-.19-.01-.82-.01-1.49-2.01.37-2.53-.49-2.69-.94-.09-.23-.48-.94-.82-1.13-.28-.15-.68-.52-.01-.53.63-.01 1.08.58 1.23.82.72 1.21 1.87.87 2.33.66.07-.52.28-.87.51-1.07-1.78-.2-3.64-.89-3.64-3.95 0-.87.31-1.59.82-2.15-.08-.2-.36-1.02.08-2.12 0 0 .67-.21 2.2.82.64-.18 1.32-.27 2-.27.68 0 1.36.09 2 .27 1.53-1.04 2.2-.82 2.2-.82.44 1.1.16 1.92.08 2.12.51.56.82 1.27.82 2.15 0 3.07-1.87 3.75-3.65 3.95.29.25.54.73.54 1.48 0 1.07-.01 1.93-.01 2.2 0 .21.15.46.55.38A8.012 8.012 0 0 0 16 8c0-4.42-3.58-8-8-8z"/>
  </svg>
  </a>
      <h1 className="text-4xl transition-shadow duration-100 md:text-6xl py-4 pb-8 md:py-10 font-bold font-sans bg-gradient-to-r from-blue-400 via-blue-800 to-indigo-800 inline-block text-transparent bg-clip-text">
        MiniSearchEngine
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
          <ul className="absolute z-10 translate-y-[40px] w-[295px] md:w-[730px] bg-white dark:bg-gray-500 mt-1 border border-gray-200 dark:border-gray-700 rounded-md max-h-60 overflow-auto">
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
      { results.length == 0 && !error &&(
      <div className="mt-2 text-center text-sky-700">
        <p className="italic">Ready to search? Let's go!</p>
      </div>)
      }
      <div className="results mt-6 flex-grow">
        {loading && <p>Loading...</p>}
        {error && <p className="error">{error}</p>}
        {!loading && !error && results.length > 0 && (
          <div>
            <ul>
              {currentResults.map((result, index) => (
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
            {totalPages > 1 && (
              <div className="pagination mt-4 flex justify-end items-center">
                <button
                  onClick={() => setCurrentPage(currentPage - 1)}
                  disabled={currentPage === 1}
                  className="nav px-2 py-1 bg-gray-200 dark:bg-gray-700 rounded disabled:opacity-50"
                >
                  {'<'}
                </button>
                <span className="mx-2 py-6">
                  Page {currentPage} of {totalPages}
                </span>
                <button
                  onClick={() => setCurrentPage(currentPage + 1)}
                  disabled={currentPage === totalPages}
                  className="nav px-2 py-1 bg-gray-200 dark:bg-gray-700 rounded disabled:opacity-50"
                >
                  {'>'}
                </button>
              </div>
            )}
          </div>
        )}
        {!loading && !error && hasSearched && results.length === 0 && (
          <p>No results found.</p>
        )}
      </div>
      <footer className="mt-10 text-center">
        <p className="text-sm pb-5 font-sans text-gray-500 dark:text-gray-300">
          Built on <img src="/spring.svg" alt="Spring Logo" className="inline-block w-6 h-6"/> & <img src="/react.svg" alt="React Logo" className="inline-block w-6 h-6"/>
        </p>
      </footer>
    </div>
  );
}

export default App;