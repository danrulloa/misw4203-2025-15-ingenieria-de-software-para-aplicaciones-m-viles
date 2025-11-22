package com.miso.vinilo.data.adapter.retrofit

import com.miso.vinilo.data.dto.AlbumDto
import com.miso.vinilo.data.dto.CommentDto
import com.miso.vinilo.data.dto.NewCommentDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AlbumApi {
    @GET("albums")
    suspend fun getAlbums(): List<AlbumDto>

    @GET("albums/{id}")
    suspend fun getAlbum(@Path("id") id: Long): AlbumDto

    @POST("albums/{id}/comments")
    suspend fun postComment(
        @Path("id") albumId: Long,
        @Body comment: NewCommentDto
    ): CommentDto
}
