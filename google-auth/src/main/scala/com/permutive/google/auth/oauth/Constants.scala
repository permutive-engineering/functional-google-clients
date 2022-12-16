/*
 * Copyright 2022 Permutive
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.permutive.google.auth.oauth

import org.http4s.Uri

private[oauth] object Constants {

  // Documentation is inconsistent
  //
  // Show as `https://accounts.google.com/o/oauth2/v2/auth` here:
  //   https://developers.google.com/identity/protocols/OAuth2WebServer
  //
  // URI below seems to be newer though
  final val googleOAuthRequestUri =
    Uri.unsafeFromString("https://oauth2.googleapis.com/token")

  // https://cloud.google.com/compute/docs/access/create-enable-service-accounts-for-instances#applications
  final val googleInstanceMetadataTokenUri = Uri.unsafeFromString(
    "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token"
  )

}
