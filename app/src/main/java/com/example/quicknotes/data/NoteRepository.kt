package com.example.quicknotes.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NoteRepository(private val dao: NoteDao) {
    fun getAll(): Flow<List<Note>> = dao.getAll()
    fun search(query: String): Flow<List<Note>> = dao.search("%query%")
    suspend fun insert(note: Note): Long = dao.insert(note)
    suspend fun update(note: Note) = dao.update(note)
    suspend fun delete(note: Note) = dao.delete(note)
    suspend fun getById(id: Long): Note? = dao.getById(id)
}