import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({

  plugins: [react()],

  /*
   * Durante lo sviluppo con npm run dev,
   * Vite inoltra le richieste /api a Spring Boot.
   */
  server: {

    proxy: {

      '/api': {

        target: 'http://localhost:8080',

        changeOrigin: true

      }

    }

  },


  /*
   * Quando facciamo npm run build,
   * React viene copiato direttamente
   * dentro le risorse statiche di Spring Boot.
   */
  base: '/react-dashboard/',

  build: {

    outDir:
      '../src/main/resources/static/react-dashboard',

    emptyOutDir: true

  }

})