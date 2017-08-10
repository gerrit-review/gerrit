// Copyright (C) 2012 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gerrit.server.plugins;

<<<<<<< HEAD   (4a6482 Disentangle ListPlugins and PluginLsCommand)
import static java.util.Comparator.comparing;

import com.google.common.collect.Streams;
=======
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gerrit.common.Nullable;
>>>>>>> BRANCH (a5420f Allow running without gerrit.canonicalWebUrl)
import com.google.gerrit.common.data.GlobalCapability;
import com.google.gerrit.extensions.annotations.RequiresCapability;
import com.google.gerrit.extensions.common.PluginInfo;
import com.google.gerrit.extensions.restapi.BadRequestException;
import com.google.gerrit.extensions.restapi.RestReadView;
import com.google.gerrit.extensions.restapi.TopLevelResource;
import com.google.gerrit.extensions.restapi.Url;
<<<<<<< HEAD   (4a6482 Disentangle ListPlugins and PluginLsCommand)
=======
import com.google.gerrit.server.OutputFormat;
import com.google.gson.reflect.TypeToken;
>>>>>>> BRANCH (a5420f Allow running without gerrit.canonicalWebUrl)
import com.google.inject.Inject;
<<<<<<< HEAD   (4a6482 Disentangle ListPlugins and PluginLsCommand)
import java.util.Locale;
=======
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
>>>>>>> BRANCH (a5420f Allow running without gerrit.canonicalWebUrl)
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.kohsuke.args4j.Option;

/** List the installed plugins. */
@RequiresCapability(GlobalCapability.VIEW_PLUGINS)
public class ListPlugins implements RestReadView<TopLevelResource> {
  private final PluginLoader pluginLoader;

  private boolean all;
  private int limit;
  private int start;
  private String matchPrefix;
  private String matchSubstring;
  private String matchRegex;

  @Option(
    name = "--all",
    aliases = {"-a"},
    usage = "List all plugins, including disabled plugins"
  )
  public void setAll(boolean all) {
    this.all = all;
  }

  @Option(
    name = "--limit",
    aliases = {"-n"},
    metaVar = "CNT",
    usage = "maximum number of plugins to list"
  )
  public void setLimit(int limit) {
    this.limit = limit;
  }

  @Option(
    name = "--start",
    aliases = {"-S"},
    metaVar = "CNT",
    usage = "number of plugins to skip"
  )
  public void setStart(int start) {
    this.start = start;
  }

  @Option(
    name = "--prefix",
    aliases = {"-p"},
    metaVar = "PREFIX",
    usage = "match plugin prefix"
  )
  public void setMatchPrefix(String matchPrefix) {
    this.matchPrefix = matchPrefix;
  }

  @Option(
    name = "--match",
    aliases = {"-m"},
    metaVar = "MATCH",
    usage = "match plugin substring"
  )
  public void setMatchSubstring(String matchSubstring) {
    this.matchSubstring = matchSubstring;
  }

  @Option(name = "-r", metaVar = "REGEX", usage = "match plugin regex")
  public void setMatchRegex(String matchRegex) {
    this.matchRegex = matchRegex;
  }

  @Inject
  protected ListPlugins(PluginLoader pluginLoader) {
    this.pluginLoader = pluginLoader;
  }

  @Override
  public SortedMap<String, PluginInfo> apply(TopLevelResource resource) throws BadRequestException {
    Stream<Plugin> s = Streams.stream(pluginLoader.getPlugins(all));
    if (matchPrefix != null) {
      checkMatchOptions(matchSubstring == null && matchRegex == null);
      s = s.filter(p -> p.getName().startsWith(matchPrefix));
    } else if (matchSubstring != null) {
      checkMatchOptions(matchPrefix == null && matchRegex == null);
      String substring = matchSubstring.toLowerCase(Locale.US);
      s = s.filter(p -> p.getName().toLowerCase(Locale.US).contains(substring));
    } else if (matchRegex != null) {
      checkMatchOptions(matchPrefix == null && matchSubstring == null);
      Pattern pattern = Pattern.compile(matchRegex);
      s = s.filter(p -> pattern.matcher(p.getName()).matches());
    }
    s = s.sorted(comparing(Plugin::getName));
    if (start > 0) {
      s = s.skip(start);
    }
    if (limit > 0) {
      s = s.limit(limit);
    }
    return new TreeMap<>(s.collect(Collectors.toMap(p -> p.getName(), p -> toPluginInfo(p))));
  }

<<<<<<< HEAD   (4a6482 Disentangle ListPlugins and PluginLsCommand)
  private void checkMatchOptions(boolean cond) throws BadRequestException {
    if (!cond) {
      throw new BadRequestException("specify exactly one of p/m/r");
=======
  public SortedMap<String, PluginInfo> display(@Nullable PrintWriter stdout) {
    SortedMap<String, PluginInfo> output = new TreeMap<>();
    List<Plugin> plugins = Lists.newArrayList(pluginLoader.getPlugins(all));
    Collections.sort(
        plugins,
        new Comparator<Plugin>() {
          @Override
          public int compare(Plugin a, Plugin b) {
            return a.getName().compareTo(b.getName());
          }
        });

    if (!format.isJson()) {
      stdout.format("%-30s %-10s %-8s %s\n", "Name", "Version", "Status", "File");
      stdout.print(
          "-------------------------------------------------------------------------------\n");
>>>>>>> BRANCH (a5420f Allow running without gerrit.canonicalWebUrl)
    }
<<<<<<< HEAD   (4a6482 Disentangle ListPlugins and PluginLsCommand)
=======

    for (Plugin p : plugins) {
      PluginInfo info = new PluginInfo(p);
      if (format.isJson()) {
        output.put(p.getName(), info);
      } else {
        stdout.format(
            "%-30s %-10s %-8s %s\n",
            p.getName(),
            Strings.nullToEmpty(info.version),
            p.isDisabled() ? "DISABLED" : "ENABLED",
            p.getSrcFile().getFileName());
      }
    }

    if (stdout == null) {
      return output;
    } else if (format.isJson()) {
      format
          .newGson()
          .toJson(output, new TypeToken<Map<String, PluginInfo>>() {}.getType(), stdout);
      stdout.print('\n');
    }
    stdout.flush();
    return null;
>>>>>>> BRANCH (a5420f Allow running without gerrit.canonicalWebUrl)
  }

  public static PluginInfo toPluginInfo(Plugin p) {
    String id;
    String version;
    String indexUrl;
    String filename;
    Boolean disabled;

    id = Url.encode(p.getName());
    version = p.getVersion();
    disabled = p.isDisabled() ? true : null;
    if (p.getSrcFile() != null) {
      indexUrl = String.format("plugins/%s/", p.getName());
      filename = p.getSrcFile().getFileName().toString();
    } else {
      indexUrl = null;
      filename = null;
    }

    return new PluginInfo(id, version, indexUrl, filename, disabled);
  }
}
