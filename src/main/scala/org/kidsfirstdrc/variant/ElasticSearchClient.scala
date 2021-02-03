package org.kidsfirstdrc.variant

import org.apache.http.client.methods.{HttpDelete, HttpGet, HttpPut}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient
import org.apache.http.util.EntityUtils
import org.apache.http.{HttpHeaders, HttpResponse}

import java.io.File

class ElasticSearchClient(url: String) {

  /**
   * Sends a GET on the url and verify the status code of the response is 200
   * @return true if running
   *         false if not running or if status code not 200
   */
  def isRunning: Boolean = {
    val response = new DefaultHttpClient().execute(new HttpGet(url))
    println(s"""
               |GET $url
               |${response.toString}
               |${EntityUtils.toString(response.getEntity)}
               |""".stripMargin)

    response.getStatusLine.getStatusCode == 200
  }

  /**
   * Check roles/http endpoint
   * @return true if running
   *         false if not running or if status code not 200
   */
  def checkNodeRoles: Boolean = {
    val nodesHttpUrl = s"$url/_nodes/http"
    val response = new DefaultHttpClient().execute(new HttpGet(nodesHttpUrl))

    println(s"""
               |GET $nodesHttpUrl
               |${response.toString}
               |${EntityUtils.toString(response.getEntity)}
               |""".stripMargin)
    response.getStatusLine.getStatusCode == 200
  }

  /**
   * Set a template to ElasticSearch
   * @param templateFileName path of the template.json that is expected to be in the resource folder
   * @param templateName name for the template
   * @return the http response sent by ElasticSearch
   */
  def setTemplate(templateFileName: String, templateName: String): HttpResponse = {

    val requestUrl = s"$url/_template/$templateName"


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

  /**
   * Delete a template
   * @param templateName name of the template to delete
   * @return the http response sent by ElasticSearch
   */
  def deleteTemplate(templateName: String): HttpResponse = {
    val requestUrl = s"$url/_template/$templateName"
    val request = new HttpDelete(requestUrl)
    val response = new DefaultHttpClient().execute(request)
    response
  }

  /**
   * Create an index
   * @param indexName name of the index to create
   * @return the http response sent by ElasticSearch
   */
  def createIndex(indexName: String): HttpResponse = {
    val requestUrl = s"$url/$indexName"
    val request = new HttpPut(requestUrl)
    val response = new DefaultHttpClient().execute(request)
    response
  }

  /**
   * Delete an index
   * @param indexName name of the index to delete
   * @return the http response sent by ElasticSearch
   */
  def deleteIndex(indexName: String): HttpResponse = {
    val requestUrl = s"$url/$indexName"
    val request = new HttpDelete(requestUrl)
    val response = new DefaultHttpClient().execute(request)
    response
  }

}
