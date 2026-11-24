package com.example.final_project.fragments

import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.final_project.EndlessRecyclerViewScrollListener
import com.example.final_project.activities.MainActivity
import com.example.final_project.adapters.EventAdapter
import com.example.final_project.models.Embedded
import com.example.final_project.models.Event
import com.example.final_project.models.EventData
import com.example.final_project.models.TMApi
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyMap
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EventViewCallbackTest {
    private class Fixture(loadMore: Boolean) {
        val fragment = spy(TMEventFragment())
        private val initialView = mock(View::class.java)
        @Suppress("UNCHECKED_CAST")
        val call = mock(Call::class.java) as Call<EventData>
        lateinit var callback: Callback<EventData>
        private val event = mock(Event::class.java)
        private val body = mock(EventData::class.java)

        init {
            // No real context, credentials, activity startup, or network request is used.
            doReturn(initialView).`when`(fragment).view
            doReturn("unit-test-placeholder").`when`(fragment).getString(anyInt())
            fragment.adapter = mock(EventAdapter::class.java)
            fragment.swipeContainer = mock(SwipeRefreshLayout::class.java)
            fragment.scrollListener = mock(EndlessRecyclerViewScrollListener::class.java)
            fragment.pageNum = 1
            val api = mock(TMApi::class.java)
            MainActivity.apiService = api
            doReturn(call).`when`(api).getEvents(anyMap())
            doAnswer { invocation ->
                callback = invocation.getArgument(0)
                null
            }.`when`(call).enqueue(any())
            doReturn(Embedded(listOf(event))).`when`(body)._embedded
            if (loadMore) fragment.loadMoreData() else fragment.queryEvents()
        }

        fun replaceView(view: View?) {
            doReturn(view).`when`(fragment).view
        }

        fun respond() {
            callback.onResponse(call, Response.success(body))
        }
    }

    @Test
    fun destroyedViewIgnoresLateRefreshAndPaginationResponses() {
        for (loadMore in listOf(false, true)) {
            val fixture = Fixture(loadMore)
            fixture.replaceView(null)
            fixture.respond()
            assertEquals(0, fixture.fragment.allEvents.size)
            verify(fixture.fragment.adapter, never()).notifyDataSetChanged()
        }
    }

    @Test
    fun recreatedViewDoesNotReceiveThePreviousViewsResponse() {
        for (loadMore in listOf(false, true)) {
            val fixture = Fixture(loadMore)
            fixture.replaceView(mock(View::class.java))
            fixture.respond()
            assertEquals(0, fixture.fragment.allEvents.size)
            verify(fixture.fragment.adapter, never()).notifyDataSetChanged()
        }
    }

    @Test
    fun currentViewStillReceivesRefreshAndPaginationResponses() {
        for (loadMore in listOf(false, true)) {
            val fixture = Fixture(loadMore)
            fixture.respond()
            assertEquals(1, fixture.fragment.allEvents.size)
            verify(fixture.fragment.adapter).notifyDataSetChanged()
        }
    }
}
