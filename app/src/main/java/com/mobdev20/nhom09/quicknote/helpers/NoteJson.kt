package com.mobdev20.nhom09.quicknote.helpers

import com.mobdev20.nhom09.quicknote.state.HistoryType
import com.mobdev20.nhom09.quicknote.state.NoteHistory
import com.mobdev20.nhom09.quicknote.state.NoteOverview
import com.mobdev20.nhom09.quicknote.state.NoteState
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import java.time.Instant

class NoteJson {
    companion object {
        fun convertModel(noteState: NoteState): String {
            val model = """
            {
                "id": "${noteState.id}",
                "userId": "${noteState.userId}",
                "title": "${Encoder.encode(noteState.title)}",
                "content": "${Encoder.encode(noteState.content)}",
                "notificationTime": "${noteState.notificationTime.toString()}",
                "notificationId": "${noteState.notificationId}",
                "timeUpdate": "${noteState.timeUpdate.toString()}",
                "timeRestore": "${noteState.timeRestore.toString()}",
                "attachmentCount": ${noteState.attachmentCount},
                "attachmentsPath": [
                """ + noteState.attachments.map {
                """ "${it}" """.trimIndent()
            }.joinToString(",\n") + """
                    ],
                "history": [
                    """ + noteState.history.map {
                """
                            {
                                "timestamp": "${it.timestamp.toString()}",
                                "userId": "${it.userId}",
                                "line": ${it.line},
                                "type": "${it.type.name}",
                                "contentOld": "${Encoder.encode(it.contentOld)}",
                                "contentNew": "${Encoder.encode(it.contentNew)}"
                            }
                        """.trimIndent()
            }.joinToString(",\n") + """
                ]
            }
        """.trimIndent()
            return model
        }

        fun convertJson(value: String): NoteState {
            val model = Json.parseToJsonElement(value.toString()).jsonObject
            return NoteState(
                id = model["id"]?.jsonPrimitive?.content ?: "",
                userId = model["userId"]?.jsonPrimitive?.content ?: "",
                title = Encoder.decode(model["title"].toString()),
                content = Encoder.decode(
                    model["content"].toString()
                ),
                attachments = model["attachmentsPath"]?.jsonArray?.map {
                    it?.jsonPrimitive?.content ?: ""
                }!!.toMutableList(),
                attachmentCount = model["attachmentCount"]?.jsonPrimitive?.long ?: 0,
                notificationTime = Instant.parse(model["notificationTime"]?.jsonPrimitive?.content),
                notificationId = model["notificationId"]?.jsonPrimitive?.content ?: "",
                timeUpdate = Instant.parse(model["timeUpdate"]?.jsonPrimitive?.content),
                timeRestore = Instant.parse(model["timeRestore"]?.jsonPrimitive?.content),
                history = model["history"]?.jsonArray?.map {
                    NoteHistory(
                        timestamp = Instant.parse(it.jsonObject["timestamp"]?.jsonPrimitive?.content),
                        userId = it.jsonObject["userId"]?.jsonPrimitive?.content ?: "",
                        line = it.jsonObject["line"]?.jsonPrimitive?.int ?: 0,
                        type = it.jsonObject["type"]?.jsonPrimitive?.content?.let { it1 ->
                            HistoryType.valueOf(
                                it1
                            )
                        } ?: HistoryType.EDIT,
                        contentOld = Encoder.decode(it.jsonObject["contentOld"]?.jsonPrimitive?.content.toString()),
                        contentNew = Encoder.decode(it.jsonObject["contentNew"]?.jsonPrimitive?.content.toString()))
                }?.toMutableList() ?: mutableListOf()
            )
        }


        fun convertPartialJson(value: String): NoteOverview {
            val model = Json.parseToJsonElement(value.toString()).jsonObject
            return NoteOverview(
                id = model["id"]?.jsonPrimitive?.content ?: "",
                title = Encoder.decode(model["title"].toString()),
                content = Encoder.decode(
                    model["content"].toString()
                ),
            )
        }

        fun getFilenameFromAttachPath(path: String): String {
            val filename = path.substring(path.lastIndexOf("/") + 1)
            val fileSpilt = filename.split(".")
            return fileSpilt[0] + "." + fileSpilt.subList(2, fileSpilt.size - 1).joinToString(".")
        }

        fun getFileNewName(filename: String, addition: String): String {
            val fileSpilt = filename.split(".").toMutableList()
            fileSpilt.add(1, addition)
            return fileSpilt.joinToString(".")
        }

        fun getLast(filepath: String): String {
            val fileLast = filepath.split(".")
            return fileLast.subList(0, fileLast.size - 1).joinToString(".")
        }
    }
}