package com.example.final_project.activities

import android.content.Intent
import android.text.Editable
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.final_project.R
import com.parse.ParseException
import com.parse.ParseUser
import com.parse.SignUpCallback
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.mockito.Mockito

class SignUpNavigationTest {
    private fun exercise(username: String, password: String, success: Boolean) {
        val activity = Mockito.mock(SignUpActivity::class.java, Mockito.CALLS_REAL_METHODS)
        val button = Mockito.mock(Button::class.java)
        val usernameField = Mockito.mock(EditText::class.java)
        val passwordField = Mockito.mock(EditText::class.java)
        val usernameText = Mockito.mock(Editable::class.java)
        val passwordText = Mockito.mock(Editable::class.java)
        Mockito.`when`(usernameText.toString()).thenReturn(username)
        Mockito.`when`(passwordText.toString()).thenReturn(password)
        Mockito.`when`(usernameField.text).thenReturn(usernameText)
        Mockito.`when`(passwordField.text).thenReturn(passwordText)
        Mockito.doReturn(button).`when`(activity).findViewById<Button>(R.id.Signup_SignUp)
        Mockito.doReturn(usernameField).`when`(activity).findViewById<EditText>(R.id.Signup_Username)
        Mockito.doReturn(passwordField).`when`(activity).findViewById<EditText>(R.id.Signup_Password)
        Mockito.doNothing().`when`(activity).startActivity(Mockito.any(Intent::class.java))
        Mockito.doNothing().`when`(activity).finish()
        var click: View.OnClickListener? = null
        Mockito.doAnswer { call ->
            click = call.getArgument(0)
            null
        }.`when`(button).setOnClickListener(Mockito.any(View.OnClickListener::class.java))
        var callback: SignUpCallback? = null
        Mockito.mockConstruction(ParseUser::class.java) { user, _ ->
            Mockito.doAnswer { call ->
                callback = call.getArgument(0)
                null
            }.`when`(user).signUpInBackground(Mockito.any(SignUpCallback::class.java))
        }.use {
            Mockito.mockStatic(Toast::class.java).use { toasts ->
                val toast = Mockito.mock(Toast::class.java)
                toasts.`when`<Toast> {
                    Toast.makeText(Mockito.eq(activity), Mockito.anyString(), Mockito.anyInt())
                }.thenReturn(toast)
                Mockito.mockConstruction(Intent::class.java).use {
                    activity.bindSignUpButton()
                    click!!.onClick(button)
                    Mockito.verify(activity, Mockito.never()).startActivity(Mockito.any(Intent::class.java))
                    Mockito.verify(activity, Mockito.never()).finish()
                    if (username.isNotEmpty() && password.isNotEmpty()) {
                        assertNotNull(callback)
                        callback!!.done(if (success) null else Mockito.mock(ParseException::class.java))
                        if (success) {
                            Mockito.verify(activity).startActivity(Mockito.any(Intent::class.java))
                            Mockito.verify(activity).finish()
                        } else {
                            Mockito.verify(activity, Mockito.never()).startActivity(Mockito.any(Intent::class.java))
                            Mockito.verify(activity, Mockito.never()).finish()
                        }
                    }
                    Mockito.verify(toast).show()
                }
            }
        }
    }

    @Test
    fun emptyCredentialsRemainOnTheSignUpScreen() {
        exercise("", "fixture-password", false)
        exercise("fixture-user", "", false)
    }

    @Test
    fun pendingAndRejectedRegistrationCannotNavigate() {
        exercise("fixture-user", "fixture-password", false)
    }

    @Test
    fun successfulRegistrationNavigatesOnlyFromTheProviderCallback() {
        exercise("fixture-user", "fixture-password", true)
    }
}
