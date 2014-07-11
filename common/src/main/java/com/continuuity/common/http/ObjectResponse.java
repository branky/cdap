/*
 * Copyright (c) 2012-2014 Continuuity Inc. All rights reserved.
 */
package com.continuuity.common.http;

import com.google.common.base.Charsets;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;

/**
 * Convenient wrapper of {@link HttpResponse} that makes client code cleaner when dealing with java object that can be
 * de-serialized from response body.
 *
 * @param <T> type of the response object
 */
public final class ObjectResponse<T> extends HttpResponse {
  private static final Gson GSON = new Gson();

  private final T object;

  @SuppressWarnings("unchecked")
  public static <T> ObjectResponse<T> fromJsonBody(HttpResponse response, Type typeOfObject, Gson gson) {
    T object = response.getResponseBody() == null ?
      null : (T) gson.fromJson(new String(response.getResponseBody(), Charsets.UTF_8), typeOfObject);
    return new ObjectResponse<T>(response, object);
  }

  @SuppressWarnings("unchecked")
  public static <T> ObjectResponse<T> fromJsonBody(HttpResponse response, Type typeOfObject) {
    return fromJsonBody(response, typeOfObject, GSON);
  }

  public static <T> ObjectResponse<T> fromJsonBody(HttpResponse response, TypeToken<T> typeOfObject, Gson gson) {
    return fromJsonBody(response, (Type) typeOfObject.getType(), gson);
  }

  public static <T> ObjectResponse<T> fromJsonBody(HttpResponse response, TypeToken<T> typeOfObject) {
    return fromJsonBody(response, (Type) typeOfObject.getType(), GSON);
  }

  public static <T> ObjectResponse<T> fromJsonBody(HttpResponse response, Class<T> typeOfObject, Gson gson) {
    return fromJsonBody(response, (Type) typeOfObject, gson);
  }

  public static <T> ObjectResponse<T> fromJsonBody(HttpResponse response, Class<T> typeOfObject) {
    return fromJsonBody(response, (Type) typeOfObject, GSON);
  }

  private ObjectResponse(HttpResponse response, T object) {
    super(response.getResponseCode(), response.getResponseMessage(), response.getResponseBody());
    this.object = object;
  }

  public T getResponseObject() {
    return object;
  }
}
