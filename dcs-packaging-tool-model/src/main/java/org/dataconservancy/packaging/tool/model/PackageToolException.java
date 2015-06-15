/*
 * Copyright 2013 Johns Hopkins University
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
package org.dataconservancy.packaging.tool.model;

/**
 * Handle various runtime exceptions thrown by services etc. involved in
 * generating packages. We track an integer code to be used as a return code for
 * command line use, as well as a brief error message, in the ReturnCode enum in PackageToolReturnInfo.
 * We allow a brief addendum to the generic error message to allow for a context-specific pointer to
 * a cause for the error.
 *
 */
public class PackageToolException extends RuntimeException{

   private int code;

   public PackageToolException(PackagingToolReturnInfo returnInfo) {
       super(returnInfo.stringMessage());
       this.code = returnInfo.returnCode();
   }
   public PackageToolException(PackagingToolReturnInfo returnInfo, String briefBlame){
       super(returnInfo.stringMessage() + ": " + briefBlame);
       this.code = returnInfo.returnCode();
   }

    public PackageToolException(PackagingToolReturnInfo returnInfo, Exception e) {
       super(returnInfo.stringMessage(), e);
       this.code = returnInfo.returnCode();
   }

   public PackageToolException(PackagingToolReturnInfo returnInfo, Exception e, String briefBlame){
       super(returnInfo.stringMessage() + ": " + briefBlame, e);
       this.code = returnInfo.returnCode();
   }

   public int getCode(){
       return code;
   }

}
