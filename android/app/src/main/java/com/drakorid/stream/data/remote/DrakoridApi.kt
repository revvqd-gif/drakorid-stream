package com.drakorid.stream.data.remote

import com.drakorid.stream.data.remote.dto.EpisodeDetailDto
import com.drakorid.stream.data.remote.dto.JumlahKomentarDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Retrofit interface for the small set of JSON endpoints.
 * All endpoints live under https://drakorid.co/. The base URL is configured in [NetworkModule].
 *
 * The drakorid.co server validates a per-render `token` field against the current HTML
 * session — see [TokenProvider] for the fetch + cache strategy.
 */
interface DrakoridApi {

    /** Returns file ID + CDN flags for a given drama episode. */
    @FormUrlEncoded
    @POST("myapi/episode_detail.php")
    suspend fun episodeDetail(
        @Field("token") token: String,
        @Field("id") dramaId: Int,
        @Field("episode") episode: Int,
    ): EpisodeDetailDto

    /** Returns total comment count for a drama. */
    @FormUrlEncoded
    @POST("myapi/jumlah_komentar.php")
    suspend fun jumlahKomentar(
        @Field("token") token: String,
        @Field("id") dramaId: Int,
    ): JumlahKomentarDto
}