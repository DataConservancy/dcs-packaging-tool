# Copyright 2015 Johns Hopkins University
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#   http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
Summary: GUI for describing data and generating data packages from file system.
Name: DCPackageTool
Version: 1.0.2
Release: 1
License: Apache License, Version 2.0
Vendor: Data Conservancy <dc-ird@googlegroups.com>
Prefix: /opt
Provides: dc-package-tool
Requires: ld-linux.so.2 libX11.so.6 libXext.so.6 libXi.so.6 libXrender.so.1 libXtst.so.6 libasound.so.2 libc.so.6 libdl.so.2 libgcc_s.so.1 libm.so.6 libpthread.so.0 libthread_db.so.1
Autoprov: 0
Autoreq: 0

#avoid ARCH subfolder
%define _rpmfilename %%{NAME}-%%{VERSION}-%%{RELEASE}.%%{ARCH}.rpm

#comment line below to enable effective jar compression
#it could easily get your package size from 40 to 15Mb but 
#build time will substantially increase and it may require unpack200/system java to install
%define __jar_repack %{nil}

%description
Describe data files on a filesystem and package them.

%prep

%build

%install
rm -rf %{buildroot}
mkdir -p %{buildroot}/opt
cp -r %{_sourcedir}/DCPackageTool %{buildroot}/opt

%files

/opt/DCPackageTool

%post
cp /opt/DCPackageTool/DCPackageTool.desktop /usr/share/applications/

%preun
rm -f /usr/share/applications/DCPackageTool.desktop

%clean
