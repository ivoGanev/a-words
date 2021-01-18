package com.ivo.ganev.awords.provider

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


class AppDocumentProvider : DocumentsProvider() {
    private val defaultDocumentProjection: Array<String> = arrayOf(
        Document.COLUMN_DOCUMENT_ID,
        Document.COLUMN_MIME_TYPE,
        Document.COLUMN_DISPLAY_NAME,
        Document.COLUMN_LAST_MODIFIED,
        Document.COLUMN_FLAGS,
        Document.COLUMN_SIZE
    )

    private val rootId: String = "root"

    // more MIME types can be added by adding a new line like:
    // "text/*\n
    //  image/*\n" and so on
    private val mimeTypes = "text/plain"

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

    private lateinit var baseDir: File

    override fun onCreate(): Boolean {
        baseDir = context!!.filesDir
        println("Loaded Documents Provider")
        createDummyDocuments()
        return true
    }

    private fun createDummyDocuments() {
        val dummyTxt = File("$baseDir/dummy.txt")
        dummyTxt.writeText("Hello World")
        val dummyMp3 = File("$baseDir/dummy.mp3")
        dummyTxt.createNewFile()
        dummyMp3.createNewFile()
    }

    override fun queryRoots(projection: Array<String>?): Cursor {
        val result = resolveRootProjection(projection)

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
                Root.FLAG_SUPPORTS_CREATE or
                        Root.FLAG_SUPPORTS_RECENTS or
                        Root.FLAG_SUPPORTS_SEARCH
            )

            // COLUMN_TITLE is the root title (e.g. Gallery, Drive).
            add(Root.COLUMN_TITLE, context?.getString(R.string.title))

            // This document id cannot change after it's shared.
            add(Root.COLUMN_DOCUMENT_ID, getDocIdForFile(baseDir))

            // The child MIME types are used to filter the roots and only present to the
            // user those roots that contain the desired type somewhere in their file hierarchy.
            add(Root.COLUMN_MIME_TYPES, mimeTypes)
            add(Root.COLUMN_ICON, R.drawable.ic_launcher_background)
        }
        return result
    }

    private fun resolveRootProjection(projection: Array<String>?) =
        MatrixCursor(projection ?: defaultRootProjection)

    override fun queryDocument(documentId: String, projection: Array<String>?): Cursor {
        val result = MatrixCursor(resolveDocumentProjection(projection))
        val file = getFileForDocId(documentId)
        includeFile(result, file)
        println("Querying single document $documentId file: ${file.path}")
        return result
    }

    override fun queryChildDocuments(parentDocumentId: String, projection: Array<String>?, sortOrder: String?): Cursor {
        println(
            "queryChildDocuments, parentDocumentId: $parentDocumentId sortOrder: $sortOrder"
        )

        val result = MatrixCursor(resolveDocumentProjection(projection))
        val parent: File = getFileForDocId(parentDocumentId)
        for (file in parent.listFiles()!!) {
            println("parent contains: " + file.path)
            includeFile(result, file)
        }
        return result
    }

    override fun openDocument(
        documentId: String,
        mode: String,
        signal: CancellationSignal?
    ): ParcelFileDescriptor {
        return ParcelFileDescriptor.open(getFileForDocId(documentId), ParcelFileDescriptor.parseMode(mode))
    }

    private fun includeFile(cursor: MatrixCursor, file: File) {
        val docId = getDocIdForFile(file)
        println("Including file in cursor: $docId")

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
                    Document.FLAG_SUPPORTS_DELETE or
                            Document.FLAG_SUPPORTS_WRITE
                )
                add(Document.COLUMN_SIZE, file.length())
            }
            add(Document.COLUMN_DISPLAY_NAME, file.name)
            add(Document.COLUMN_LAST_MODIFIED, file.lastModified())
        }
    }

    /**
     * Translate your custom URI scheme into a File object.
     *
     * @param docId the document ID representing the desired file
     * @return a File represented by the given document ID
     * @throws java.io.FileNotFoundException
     */
    @Throws(FileNotFoundException::class)
    private fun getFileForDocId(docId: String): File {
        var target: File = baseDir
        if (docId == rootId) {
            return target
        }
        val splitIndex = docId.indexOf(':', 1)
        return if (splitIndex < 0) {
            throw FileNotFoundException("Missing root for $docId")
        } else {
            val path = docId.substring(splitIndex + 1)
            println("File path from docId = $path")
            target = File(target, path)
            if (!target.exists()) {
                throw FileNotFoundException("Missing file for $docId at $target")
            }
            target
        }
    }

    private fun resolveDocumentProjection(projection: Array<String>?): Array<String> {
        return projection ?: defaultDocumentProjection
    }

    /**
     * Get the document ID given a File.  The document id must be consistent across time.  Other
     * applications may save the ID and use it to reference documents later.
     *
     *
     * This implementation is specific to this demo.  It assumes only one root and is built
     * directly from the file structure.  However, it is possible for a document to be a child of
     * multiple directories (for example "android" and "images"), in which case the file must have
     * the same consistent, unique document ID in both cases.
     *
     * @param file the File whose document ID you want
     * @return the corresponding document ID
     */
    private fun getDocIdForFile(file: File): String? {
        var path: String = file.absolutePath

        // Start at first char of path under root
        val rootPath: String = baseDir.path
        path = when {
            rootPath == path -> ""
            rootPath.endsWith("/") -> path.substring(rootPath.length)
            else -> path.substring(rootPath.length + 1)
        }
        println("$rootId:$path")
        return "$rootId:$path"
    }
}