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

        fun convertJson(value: String) : NoteState {
            val model = Json.parseToJsonElement(value.toString()).jsonObject
            return NoteState(
                id = model["id"]?.jsonPrimitive?.content ?: "",
                userId = model["userId"]?.jsonPrimitive?.content ?: "",
                title = Encoder.decode(model["title"].toString()),
                content = Encoder.decode(
                    model["content"].toString()
                ),
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
                        contentNew = Encoder.decode(it.jsonObject["timeStamp"]?.jsonPrimitive?.content.toString())
                    )
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
    }
}