package com.example.final_project.fragments

import android.view.View
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.final_project.adapters.ParseEventAdapter
import com.example.final_project.models.ParseEvent
import com.parse.FindCallback
import com.parse.ParseQuery
import com.parse.ParseUser
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import java.io.Closeable

class ParseViewCallbackTest {
    private class Fixture(loadMore: Boolean) : Closeable {
        private val users = mockStatic(ParseUser::class.java)
        private val queries = mockStatic(ParseQuery::class.java)
        val fragment = spy(ParseEventFragment())
        @Suppress("UNCHECKED_CAST")
        private val query = mock(ParseQuery::class.java) as ParseQuery<ParseEvent>
        lateinit var callback: FindCallback<ParseEvent>

        init {
            users.`when`<ParseUser> { ParseUser.getCurrentUser() }
                .thenReturn(mock(ParseUser::class.java))
            queries.`when`<ParseQuery<ParseEvent>> { ParseQuery.getQuery(ParseEvent::class.java) }
                .thenReturn(query)
            doReturn(mock(View::class.java)).`when`(fragment).view
            fragment.parseAdapter = mock(ParseEventAdapter::class.java)
            fragment.swipeContainer = mock(SwipeRefreshLayout::class.java)
            doAnswer { invocation ->
                callback = invocation.getArgument(0)
                null
            }.`when`(query).findInBackground(any<FindCallback<ParseEvent>>())
            fragment.queryEvents()
            if (loadMore) fragment.loadMoreData()
        }

        fun replaceView(view: View?) {
            doReturn(view).`when`(fragment).view
        }

        fun respond() {
            callback.done(listOf(mock(ParseEvent::class.java)), null)
        }

        override fun close() {
            queries.close()
            users.close()
        }
    }

    @Test
    fun destroyedViewIgnoresLateRefreshAndPaginationResponses() {
        for (loadMore in listOf(false, true)) {
            Fixture(loadMore).use { fixture ->
                fixture.replaceView(null)
                fixture.respond()
                verify(fixture.fragment.parseAdapter, never()).notifyDataSetChanged()
            }
        }
    }

    @Test
    fun recreatedViewDoesNotReceiveThePreviousViewsResponse() {
        for (loadMore in listOf(false, true)) {
            Fixture(loadMore).use { fixture ->
                fixture.replaceView(mock(View::class.java))
                fixture.respond()
                verify(fixture.fragment.parseAdapter, never()).notifyDataSetChanged()
            }
        }
    }

    @Test
    fun currentViewStillReceivesRefreshAndPaginationResponses() {
        for (loadMore in listOf(false, true)) {
            Fixture(loadMore).use { fixture ->
                fixture.respond()
                verify(fixture.fragment.parseAdapter).notifyDataSetChanged()
            }
        }
    }
}
