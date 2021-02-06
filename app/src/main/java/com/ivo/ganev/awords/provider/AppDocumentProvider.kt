package com.ivo.ganev.awords.provider

import android.content.Intent
import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract.Document
import android.provider.DocumentsContract.Root
import android.provider.DocumentsProvider
import com.ivo.ganev.awords.R
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException

typealias StorageAccessFramework = AppDocumentProvider.Companion
class AppDocumentProvider : DocumentsProvider() {
    companion object {
        fun getOpenIntent() = createIntent(Intent.ACTION_OPEN_DOCUMENT)
        fun getCreateIntent() = createIntent(Intent.ACTION_CREATE_DOCUMENT)

        private fun createIntent(intentCode: String): Intent =
            Intent(intentCode).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/plain"
            }
    }

    private val defaultDocumentProjection: Array<String> = arrayOf(
        Document.COLUMN_DOCUMENT_ID,
        Document.COLUMN_MIME_TYPE,
        Document.COLUMN_DISPLAY_NAME,
        Document.COLUMN_LAST_MODIFIED,
        Document.COLUMN_FLAGS,
        Document.COLUMN_SIZE
    )

    private val defaultRootProjection: Array<String> = arrayOf(
        Root.COLUMN_ROOT_ID,
        Root.COLUMN_MIME_TYPES,
        Root.COLUMN_FLAGS,
        Root.COLUMN_ICON,
        Root.COLUMN_TITLE,
        Root.COLUMN_SUMMARY,
        Root.COLUMN_DOCUMENT_ID,
        Root.COLUMN_AVAILABLE_BYTES
    )

    private val rootId: String = "root"

    // mime support is only for text
    private val mimeTypes = "text/plain"

    private val baseDir: File by lazy {
        context!!.filesDir
    }

    override fun createDocument(parentDocumentId: String, mimeType: String, displayName: String): String {
        val parent: File = getFileForDocId(parentDocumentId)
        val fileName = displayName.toTextFileFormat()

        val file: File = try {
            File(parent.path, fileName).apply {
                createNewFile()
                setWritable(true)
                setReadable(true)
            }
        } catch (ex: IOException) {
            throw FileNotFoundException(
                "Failed to create document with name $displayName and documentId $parentDocumentId"
            )
        }
        return getDocIdForFile(file)
    }

    override fun onCreate(): Boolean = true

    @Throws(FileNotFoundException::class)
    override fun deleteDocument(documentId: String) = getFileForDocId(documentId).delete().let {
        if (!it) throw FileNotFoundException("Failed to delete document with id: $documentId")
    }

    override fun openDocument(documentId: String, mode: String, signal: CancellationSignal?)
            : ParcelFileDescriptor = ParcelFileDescriptor.open(
        getFileForDocId(documentId),
        ParcelFileDescriptor.parseMode(mode)
    )

    override fun queryRoots(projection: Array<String>?): Cursor {
        val result = MatrixCursor(projection ?: defaultRootProjection)

        result.newRow().apply {
            add(Root.COLUMN_ROOT_ID, rootId)

            // You can provide an optional summary, which helps distinguish roots
            // with the same title. You can also use this field for displaying an
            // user account name.
            add(Root.COLUMN_SUMMARY, context?.getString(R.string.document_provider_root_summary))

            // FLAG_SUPPORTS_CREATE means at least one directory under the root supports
            // creating documents. FLAG_SUPPORTS_RECENTS means your application's most
            // recently used documents will show up in the "Recents" category.
            // FLAG_SUPPORTS_SEARCH allows users to search all documents the application
            // shares.
            add(
                Root.COLUMN_FLAGS,
                Root.FLAG_SUPPORTS_CREATE or Root.FLAG_SUPPORTS_RECENTS or Root.FLAG_LOCAL_ONLY
            )

            // COLUMN_TITLE is the root title (e.g. Gallery, Drive).
            add(Root.COLUMN_TITLE, context?.getString(R.string.document_provider_title))

            // This document id cannot change after it's shared.
            add(Root.COLUMN_DOCUMENT_ID, getDocIdForFile(baseDir))
            // The child MIME types are used to filter the roots and only present to the
            // user those roots that contain the desired type somewhere in their file hierarchy.
            add(Root.COLUMN_MIME_TYPES, mimeTypes)
            add(Root.COLUMN_ICON, R.drawable.ic_launcher_background)
        }
        return result
    }

    override fun queryDocument(documentId: String, projection: Array<String>?): Cursor {
        val result = MatrixCursor(projection ?: defaultDocumentProjection)
        val file = getFileForDocId(documentId)
        includeFile(result, file)
        return result
    }

    override fun queryChildDocuments(parentDocumentId: String, projection: Array<String>?, sortOrder: String?): Cursor {
        val result = MatrixCursor(projection ?: defaultDocumentProjection)
        val parent = getFileForDocId(parentDocumentId)
        for (file in parent.listFiles()!!) {
            includeFile(result, file)
        }
        return result
    }


    /**
     * This is what will make the file to appear in the Document Provider UI
     * */
    private fun includeFile(cursor: MatrixCursor, file: File) {
        val docId = getDocIdForFile(file)

        val isDirectory = file.isDirectory
        val isTextFile = file.name.endsWith(".txt")

        if (!isDirectory && !isTextFile) {
            // We don't want anything besides text files and directories floating around
            file.delete()
            return
        }

        cursor.newRow().apply {
            add(Document.COLUMN_DOCUMENT_ID, docId)
            if (isDirectory) {
                add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR)
                add(Document.COLUMN_FLAGS, Document.FLAG_DIR_SUPPORTS_CREATE)
                add(Document.COLUMN_SIZE, 0)
            } else if (isTextFile) {
                add(Document.COLUMN_MIME_TYPE, "text/plain")
                add(
                    Document.COLUMN_FLAGS,
                    Document.FLAG_SUPPORTS_DELETE or Document.FLAG_SUPPORTS_WRITE or Document.FLAG_DIR_SUPPORTS_CREATE
                )
                add(Document.COLUMN_SIZE, file.length())
            }
            add(Document.COLUMN_DISPLAY_NAME, file.name)
            add(Document.COLUMN_LAST_MODIFIED, file.lastModified())
        }
    }

    /**
     * Translate your custom URI scheme into a File object.
     */
    @Throws(FileNotFoundException::class)
    private fun getFileForDocId(docId: String): File {
        // Objective Example: Convert a document with docId being "root:file.txt",
        // to /data/user/0/com.ivo.ganev.awords/files/file.txt

        // if docId is "root:" we just return
        // /data/user/0/com.ivo.ganev.awords/files/ because it is our root directory.
        if (docId == rootId) {
            return baseDir
        }

        var target: File = baseDir

        // else when a docId comes as "root:something" we:
        // remove the "root:" and leave "something"
        val splitIndex = docId.indexOf(':', 1)
        return if (splitIndex < 0) {
            throw FileNotFoundException("Missing root for $docId")
        } else {
            val path = docId.substring(splitIndex + 1)
            target = File(target, path)

            if (!target.exists()) {
                throw FileNotFoundException("Missing file for $docId at $target")
            }
            target
        }
    }

    /**
     * Translates your file to document ID
     * */
    private fun getDocIdForFile(file: File): String {
        var absolutePath: String = file.absolutePath

        val rootPath: String = baseDir.path

        absolutePath = when {
            rootPath == absolutePath -> ""
            rootPath.endsWith("/") -> absolutePath.substring(rootPath.length)
            else -> absolutePath.substring(rootPath.length + 1)
        }
        return "$rootId:$absolutePath"
    }

    /**
     * Creates a new string from this one and adds ".txt" to the end of it.
     * */
    private fun String.toTextFileFormat(): String {
        if (!this.endsWith(".txt"))
            return "$this.txt"
        return this
    }
}