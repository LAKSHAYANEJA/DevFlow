/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',

  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
      },
      colors: {
        primary: {
          50: '#E6F0FF',
          100: '#CCE0FF',
          500: '#0052CC',
          600: '#0043A6',
          700: '#003380',
    },
    dark: {
      bg: '#0D1117',
      surface: '#161B22',
      border: '#30363D',
      text: '#E6EDF3',
    }
  }
},
},
  plugins: [],
}

