// vite.config.js
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
    plugins: [vue()],
    root: process.cwd(),
    base: './',
    server: {
        host: 'localhost',
        port: 3000,  // 明确指定3000端口
        strictPort: true,  // 如果3000被占用则报错
        open: true,  // 自动打开浏览器
        cors: true,
        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                rewrite: (path) => path.replace(/^\/api/, '')
            },
            '/ws': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                ws: true
            }
        }
    },
    resolve: {
        alias: {
            '@': resolve(__dirname, 'src')
        }
    },
    build: {
        outDir: 'dist',
        sourcemap: false
    }
})