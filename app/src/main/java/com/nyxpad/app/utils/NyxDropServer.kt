package com.nyxpad.app.utils

import java.io.BufferedOutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import kotlin.random.Random

/**
 * NyxDrop is a tiny, local-only web server. It lets a desktop or another phone
 * on the same Wi-Fi open a note in a normal browser without installing NyxPad,
 * accounts, or a cloud service. The server listens on a random port behind a
 * random path token and shuts itself down after ten minutes.
 */
class NyxDropSession internal constructor(
    val url: String,
    private val serverSocket: ServerSocket,
    private val running: AtomicBoolean,
    private val worker: Thread,
) {
    fun stop() {
        if (!running.compareAndSet(true, false)) return
        runCatching { serverSocket.close() }
        worker.interrupt()
    }
}

object NyxDropServer {
    private const val SESSION_MINUTES = 10L

    fun start(title: String, content: String): NyxDropSession? {
        val address = localAddress() ?: return null
        val server = runCatching { ServerSocket(0) }.getOrNull() ?: return null
        val alphabet = "abcdefghjkmnpqrstuvwxyz23456789"
        val token = buildString {
            repeat(10) {
                append(alphabet[Random.nextInt(alphabet.length)])
            }
        }
        val running = AtomicBoolean(true)
        val basePath = "/$token/"
        val url = "http://$address:${server.localPort}$basePath"
        val worker = thread(name = "nyxdrop", isDaemon = true) {
            while (running.get()) {
                val socket = runCatching { server.accept() }.getOrNull() ?: break
                runCatching { respond(socket, title, content, basePath) }
                runCatching { socket.close() }
            }
        }
        val session = NyxDropSession(url, server, running, worker)
        thread(name = "nyxdrop-timeout", isDaemon = true) {
            runCatching { Thread.sleep(SESSION_MINUTES * 60_000L) }
            session.stop()
        }
        return session
    }

    private fun respond(socket: Socket, title: String, content: String, basePath: String) {
        socket.soTimeout = 5_000
        val reader = socket.getInputStream().bufferedReader(StandardCharsets.US_ASCII)
        val requestLine = reader.readLine().orEmpty()
        while (true) {
            val line = reader.readLine() ?: break
            if (line.isEmpty()) break
        }
        val path = requestLine.split(' ').getOrNull(1)?.substringBefore('?').orEmpty()
        val safeTitle = title.ifBlank { "Untitled note" }
        val result = when (path) {
            "${basePath}note.md" -> Response(
                contentType = "text/markdown; charset=utf-8",
                body = "# $safeTitle\n\n$content\n",
                downloadName = "note.md",
            )

            "${basePath}note.txt" -> Response(
                contentType = "text/plain; charset=utf-8",
                body = if (title.isBlank()) content else "$safeTitle\n\n$content",
                downloadName = "note.txt",
            )

            basePath, basePath.dropLast(1) -> Response(
                contentType = "text/html; charset=utf-8",
                body = htmlPage(safeTitle, content),
                downloadName = null,
            )

            else -> Response(
                contentType = "text/plain; charset=utf-8",
                body = "Not found",
                downloadName = null,
                status = "404 Not Found",
            )
        }
        val bytes = result.body.toByteArray(StandardCharsets.UTF_8)
        val headers = buildString {
            append("HTTP/1.1 ${result.status}\r\n")
            append("Content-Type: ${result.contentType}\r\n")
            append("Content-Length: ${bytes.size}\r\n")
            append("Cache-Control: no-store\r\n")
            append("X-Content-Type-Options: nosniff\r\n")
            result.downloadName?.let {
                append("Content-Disposition: attachment; filename=\"$it\"\r\n")
            }
            append("Connection: close\r\n")
            append("\r\n")
        }
        val output = BufferedOutputStream(socket.getOutputStream())
        output.write(headers.toByteArray(StandardCharsets.US_ASCII))
        output.write(bytes)
        output.flush()
    }

    private fun htmlPage(title: String, content: String): String = """
        <!doctype html>
        <html lang="en">
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=device-width, initial-scale=1">
          <title>${escape(title)} · NyxDrop</title>
          <style>
            :root { color-scheme: dark; }
            * { box-sizing: border-box; }
            body {
              margin: 0;
              min-height: 100vh;
              background: radial-gradient(circle at 90% 5%, #3b2a66 0, #08080f 46%, #050508 100%);
              color: #e8e8f0;
              font-family: ui-sans-serif, system-ui, -apple-system, "Segoe UI", sans-serif;
              padding: 32px 18px 56px;
            }
            main {
              width: min(760px, 100%);
              margin: 0 auto;
              padding: 28px;
              border-radius: 24px;
              border: 1px solid rgba(255, 255, 255, 0.14);
              background: rgba(255, 255, 255, 0.06);
              box-shadow: 0 24px 80px rgba(0, 0, 0, 0.45);
              backdrop-filter: blur(18px);
            }
            .eyebrow {
              margin: 0 0 10px;
              font-size: 12px;
              letter-spacing: 0.18em;
              text-transform: uppercase;
              color: #b9a8ff;
            }
            h1 { margin: 0 0 22px; font-size: clamp(24px, 5vw, 38px); line-height: 1.15; }
            article {
              white-space: pre-wrap;
              overflow-wrap: anywhere;
              font-size: 16px;
              line-height: 1.65;
              color: #d5d5e4;
              padding-top: 18px;
              border-top: 1px solid rgba(255, 255, 255, 0.12);
            }
            footer { margin-top: 26px; display: flex; flex-wrap: wrap; gap: 10px; }
            a {
              color: #0b0b12;
              background: #b9a8ff;
              text-decoration: none;
              font-weight: 600;
              font-size: 14px;
              padding: 10px 14px;
              border-radius: 12px;
            }
            a.secondary { background: rgba(255, 255, 255, 0.12); color: #e8e8f0; }
          </style>
        </head>
        <body>
          <main>
            <p class="eyebrow">NyxDrop · NyxPad</p>
            <h1>${escape(title)}</h1>
            <article>${escape(content)}</article>
            <footer>
              <a href="note.md">Download Markdown</a>
              <a class="secondary" href="note.txt">Plain text</a>
            </footer>
          </main>
        </body>
        </html>
    """.trimIndent()

    private fun escape(value: String): String = buildString(value.length + 16) {
        value.forEach { character ->
            when (character) {
                '&' -> append("&amp;")
                '<' -> append("&lt;")
                '>' -> append("&gt;")
                '"' -> append("&quot;")
                '\'' -> append("&#39;")
                else -> append(character)
            }
        }
    }

    private fun localAddress(): String? = runCatching {
        NetworkInterface.getNetworkInterfaces()
            .toList()
            .asSequence()
            .filter { it.isUp && !it.isLoopback }
            .flatMap { it.inetAddresses.toList().asSequence() }
            .filterIsInstance<Inet4Address>()
            .firstOrNull { it.isSiteLocalAddress }
            ?.hostAddress
    }.getOrNull()

    private data class Response(
        val contentType: String,
        val body: String,
        val downloadName: String?,
        val status: String = "200 OK",
    )
}
