package org.kidsfirstdrc.variant

import org.apache.http.client.methods.{HttpDelete, HttpPut}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.{HttpHeaders, HttpResponse}

import java.io.File

class ElasticSearchClient(url: String) {

  def setTemplate(templateFileName: String, templateName: String): HttpResponse = {

    val requestUrl = s"$url/_index_template/$templateName"


    val path = getClass.getClassLoader.getResource(templateFileName).getPath
    if (!new File(path).exists()) throw new Exception(s"File not found: [$path]")
    val src = scala.io.Source.fromFile(new File(path))
    val fileContent = src.getLines().mkString("")
    src.close()

    println(s"SENDING: PUT $requestUrl with content: $fileContent")

    val request = new HttpPut(requestUrl)
    request.addHeader(HttpHeaders.CONTENT_TYPE,"application/json")
    request.setEntity(new StringEntity(fileContent))
    val response = new DefaultHttpClient().execute(request)
    val status = response.getStatusLine
    if (!status.getStatusCode.equals(200))
      throw new Exception(s"Server could not set template [$templateFileName] and replied :${status.getStatusCode + " : " + status.getReasonPhrase}")
    response
  }

  def deleteTemplate(templateName: String): HttpResponse = {
    val requestUrl = s"$url/_index_template/$templateName"
    val request = new HttpDelete(requestUrl)
    val response = new DefaultHttpClient().execute(request)
    response
  }

  def createIndex(indexName: String): HttpResponse = {
    val requestUrl = s"$url/$indexName"
    val request = new HttpPut(requestUrl)
    val response = new DefaultHttpClient().execute(request)
    response
  }

  def deleteIndex(indexName: String): HttpResponse = {
    val requestUrl = s"$url/$indexName"
    val request = new HttpDelete(requestUrl)
    val response = new DefaultHttpClient().execute(request)
    response
  }

}
