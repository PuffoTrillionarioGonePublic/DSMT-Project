package it.unipi.dsmt.microservices.erldbadmin.advice

import com.erldb.ErldbException
import it.unipi.dsmt.microservices.erldbadmin.dto.error.ErrorResponse
import it.unipi.dsmt.microservices.erldbadmin.exception.RequestException
import it.unipi.dsmt.microservices.erldbadmin.exception.TransparentException
import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.http.converter.HttpMessageNotWritableException
import org.springframework.security.authentication.*
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.HttpSessionRequiredException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.ServletRequestBindingException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.async.AsyncRequestTimeoutException
import org.springframework.web.multipart.support.MissingServletRequestPartException
import org.springframework.web.servlet.NoHandlerFoundException

@RestControllerAdvice
open class ExceptionAdvice {

    @ExceptionHandler(RequestException::class)
    open fun userNotFoundHandler(ex: RequestException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(ex.status).body(ErrorResponse(error = ex.error))
    }

    @ExceptionHandler(BadCredentialsException::class)
    open fun badCredentialsHandler(ex: BadCredentialsException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse(error = "Invalid credentials"))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    open fun argumentNotValidHandler(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errorMsg = ex.fieldError?.let { "${it.field}: ${it.defaultMessage}" } ?: "Invalid request"
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse(error = errorMsg))
    }


    @ExceptionHandler(org.springframework.security.access.AccessDeniedException::class)
    open fun handleAccessDeniedException(e: org.springframework.security.access.AccessDeniedException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.FORBIDDEN).body(ErrorResponse(error = "Access denied"))

    @ExceptionHandler(AccountExpiredException::class)
    open fun handleAccountExpiredException(e: AccountExpiredException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse(error = "Account expired"))

    @ExceptionHandler(AuthenticationCredentialsNotFoundException::class)
    open fun handleAuthenticationCredentialsNotFoundException(e: AuthenticationCredentialsNotFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(ErrorResponse(error = "Authentication credentials not found"))


    @ExceptionHandler(DisabledException::class)
    open fun handleDisabledException(e: DisabledException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse(error = "Account is disabled"))


    @ExceptionHandler(InsufficientAuthenticationException::class)
    open fun handleInsufficientAuthenticationException(e: InsufficientAuthenticationException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse(error = "Insufficient authentication"))

    @ExceptionHandler(LockedException::class)
    open fun handleLockedException(e: LockedException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse(error = "Account is locked"))


    @ExceptionHandler(MissingRequestHeaderException::class)
    open fun handleMissingRequestHeaderException(e: MissingRequestHeaderException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(error = "Missing header: ${e.headerName}"))

    @ExceptionHandler(HttpSessionRequiredException::class)
    open fun handleHttpSessionRequiredException(e: HttpSessionRequiredException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ErrorResponse(error = "Session is required"))

    /*@ExceptionHandler(MethodArgumentNotValidException::class)
    open fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(error = "Method argument not valid: ${e.bindingResult}"))
*/

    @ExceptionHandler(TransparentException::class)
    open fun handleTransparentException(e: TransparentException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(error = e.message ?: "Error"))



    @ExceptionHandler(MissingServletRequestParameterException::class)
    open fun handleMissingServletRequestParameterException(e: MissingServletRequestParameterException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(error = "Missing request parameter: ${e.parameterName}"))

    @ExceptionHandler(TypeMismatchException::class)
    open fun handleTypeMismatchException(e: TypeMismatchException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(error = "Type mismatch: ${e.value}"))

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    open fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(ErrorResponse(error = "HTTP request method not supported"))

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    open fun handleHttpMediaTypeNotSupportedException(e: HttpMediaTypeNotSupportedException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(ErrorResponse(error = "HTTP media type not supported"))

    @ExceptionHandler(HttpMediaTypeNotAcceptableException::class)
    open fun handleHttpMediaTypeNotAcceptableException(e: HttpMediaTypeNotAcceptableException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(ErrorResponse(error = "HTTP media type not acceptable"))

    @ExceptionHandler(MissingServletRequestPartException::class)
    open fun handleMissingServletRequestPartException(e: MissingServletRequestPartException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(error = "Missing servlet request part: ${e.requestPartName}"))

    @ExceptionHandler(NoHandlerFoundException::class)
    open fun handleNoHandlerFoundException(e: NoHandlerFoundException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse(error = "Not found"))

    @ExceptionHandler(AsyncRequestTimeoutException::class)
    open fun handleAsyncRequestTimeoutException(e: AsyncRequestTimeoutException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(ErrorResponse(error = "Async request timed out"))

    @ExceptionHandler(HttpMessageNotReadableException::class)
    open fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(error = "HTTP message not correct"))

    @ExceptionHandler(HttpMessageNotWritableException::class)
    open fun handleHttpMessageNotWritableException(e: HttpMessageNotWritableException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse(error = "HTTP message not writable"))

    @ExceptionHandler(ServletRequestBindingException::class)
    open fun handleServletRequestBindingException(e: ServletRequestBindingException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(error = "Servlet request binding problem"))

    @ExceptionHandler(ErldbException::class)
    open fun handleErldbException(e: ErldbException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.OK).body(ErrorResponse(error = (e.message ?: "Error with the database")))

    @ExceptionHandler(IllegalArgumentException::class)
    open fun handleIllegalArgumentException(e: IllegalArgumentException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(error = "Illegal argument"))

    @ExceptionHandler(IllegalStateException::class)
    open fun handleIllegalStateException(e: IllegalStateException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(ErrorResponse(error = "Illegal state"))


    @ExceptionHandler(ClassCastException::class)
    open fun handleClassCastException(e: ClassCastException): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse(error = "Type mismatch"))

    @ExceptionHandler(Exception::class)
    open fun handleAllExceptions(e: Exception): ResponseEntity<ErrorResponse> {
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        val bodyOfResponse = ErrorResponse(error = (e.message ?: "An unexpected error occurred"))
        return ResponseEntity.status(status).body(bodyOfResponse)
    }


}