package org.apache.maven.dist.tools.branches;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.maven.dist.tools.JsoupRetry;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Generate report with build status of the Jenkins job for the master branch of every Git repository in
 * <a href="https://ci-builds.apache.org/job/Maven/job/maven-box/">{@code maven-box} Apache Hosted Git Folder job</a>.
 *
 * @author Robert Scholte
 */
@Mojo( name = "list-branches", requiresProject = false )
public class ListBranchesMojo extends AbstractMavenReport
{
    private String gitboxUrl = "https://gitbox.apache.org/repos/asf";
    private String mavenboxJobsBaseUrl = "https://ci-builds.apache.org/job/Maven/job/maven-box/";
    
    private Collection<String> excluded = Arrays.asList( "maven-integration-testing", // runs with Maven core job
                                                         "maven-jenkins-env",
                                                         "maven-jenkins-lib",
                                                         "maven-sources",
                                                         "maven-studies" );
    
    private static final Map<String, String> JIRAPROJECTS = new HashMap<>();
    
    static
    {
        JIRAPROJECTS.put( "maven", "MNG" );
        JIRAPROJECTS.put( "maven-acr-plugin", "MACR" );
        JIRAPROJECTS.put( "maven-antrun-plugin", "MANTRUN" );
        JIRAPROJECTS.put( "maven-apache-parent", "MPOM" );
        JIRAPROJECTS.put( "maven-archetype", "ARCHETYPE" );
        JIRAPROJECTS.put( "maven-archetypes", "ARCHETYPE" );
        JIRAPROJECTS.put( "maven-archiver", "MSHARED" );
        JIRAPROJECTS.put( "maven-artifact-plugin", "MARTIFACT" );
        JIRAPROJECTS.put( "maven-artifact-transfer", "MSHARED" );
        JIRAPROJECTS.put( "maven-assembly-plugin", "MASSEMBLY" );
        JIRAPROJECTS.put( "maven-changelog-plugin", "MCHANGELOG" );
        JIRAPROJECTS.put( "maven-changes-plugin", "MCHANGES" );
        JIRAPROJECTS.put( "maven-checkstyle-plugin", "MCHECKSTYLE" );
        JIRAPROJECTS.put( "maven-clean-plugin", "MCLEAN" );
        JIRAPROJECTS.put( "maven-common-artifact-filters", "MSHARED" );
        JIRAPROJECTS.put( "maven-compiler-plugin", "MCOMPILER" );
        JIRAPROJECTS.put( "maven-default-skin", "MSKINS" );
        JIRAPROJECTS.put( "maven-dependency-analyzer", "MSHARED" );
        JIRAPROJECTS.put( "maven-dependency-plugin", "MDEP" );
        JIRAPROJECTS.put( "maven-dependency-tree", "MSHARED" );
        JIRAPROJECTS.put( "maven-deploy-plugin", "MDEPLOY" );
        JIRAPROJECTS.put( "maven-doap-plugin", "MDOAP" );
        JIRAPROJECTS.put( "maven-docck-plugin", "MDOCCK" );
        JIRAPROJECTS.put( "maven-doxia", "DOXIA" );
        JIRAPROJECTS.put( "maven-doxia-book-maven-plugin", "DOXIA" );
        JIRAPROJECTS.put( "maven-doxia-book-renderer", "DOXIA" );
        JIRAPROJECTS.put( "maven-doxia-converter", "DOXIATOOLS" );
        JIRAPROJECTS.put( "maven-doxia-linkcheck", "DOXIATOOLS" );
        JIRAPROJECTS.put( "maven-doxia-site", "DOXIA" );
        JIRAPROJECTS.put( "maven-doxia-sitetools", "DOXIASITETOOLS" );
        JIRAPROJECTS.put( "maven-ear-plugin", "MEAR" );
        JIRAPROJECTS.put( "maven-ejb-plugin", "MEJB" );
        JIRAPROJECTS.put( "maven-enforcer", "MENFORCER" );
        JIRAPROJECTS.put( "maven-file-management", "MSHARED" );
        JIRAPROJECTS.put( "maven-filtering", "MSHARED" );
        JIRAPROJECTS.put( "maven-fluido-skin", "MSKINS" );
        JIRAPROJECTS.put( "maven-gpg-plugin", "MGPG" );
        JIRAPROJECTS.put( "maven-help-plugin", "MHELP" );
        JIRAPROJECTS.put( "maven-indexer", "MINDEXER" );
        JIRAPROJECTS.put( "maven-install-plugin", "MINSTALL" );
        JIRAPROJECTS.put( "maven-invoker", "MSHARED" );
        JIRAPROJECTS.put( "maven-invoker-plugin", "MINVOKER" );
        JIRAPROJECTS.put( "maven-jar-plugin", "MJAR" );
        JIRAPROJECTS.put( "maven-jarsigner", "MSHARED" );
        JIRAPROJECTS.put( "maven-jarsigner-plugin", "MJARSIGNER" );
        JIRAPROJECTS.put( "maven-javadoc-plugin", "MJAVADOC" );
        JIRAPROJECTS.put( "maven-jdeprscan-plugin", "MJDEPSCAN" );
        JIRAPROJECTS.put( "maven-jdeps-plugin", "MJDEPS" );
        JIRAPROJECTS.put( "maven-jlink-plugin", "MJLINK" );
        JIRAPROJECTS.put( "maven-jmod-plugin", "MJMOD" );
        JIRAPROJECTS.put( "maven-jxr", "JXR" );
        JIRAPROJECTS.put( "maven-linkcheck-plugin", "MLINKCHECK" );
        JIRAPROJECTS.put( "maven-parent", "MPOM" );
        JIRAPROJECTS.put( "maven-patch-plugin", "MPATCH" );
        JIRAPROJECTS.put( "maven-pdf-plugin", "MPDF" );
        JIRAPROJECTS.put( "maven-plugin-testing", "MPLUGINTESTING" );
        JIRAPROJECTS.put( "maven-plugin-tools", "MPLUGIN" );
        JIRAPROJECTS.put( "maven-pmd-plugin", "MPMD" );
        JIRAPROJECTS.put( "maven-project-info-reports-plugin", "MPIR" );
        JIRAPROJECTS.put( "maven-project-utils", "MSHARED" );
        JIRAPROJECTS.put( "maven-rar-plugin", "MRAR" );
        JIRAPROJECTS.put( "maven-release", "MRELEASE" );
        JIRAPROJECTS.put( "maven-remote-resources-plugin", "MRRESOURCES" );
        JIRAPROJECTS.put( "maven-reporting-api", "MSHARED" );
        JIRAPROJECTS.put( "maven-reporting-exec", "MSHARED" );
        JIRAPROJECTS.put( "maven-reporting-impl", "MSHARED" );
        JIRAPROJECTS.put( "maven-resolver", "MRESOLVER" );
        JIRAPROJECTS.put( "maven-resolver-ant-tasks", "MRESOLVER" );
        JIRAPROJECTS.put( "maven-resources-plugin", "MRESOURCES" );
        JIRAPROJECTS.put( "maven-scm", "SCM" );
        JIRAPROJECTS.put( "maven-scm-publish-plugin", "MSCMPUB" );
        JIRAPROJECTS.put( "maven-script-interpreter", "MSHARED" );
        JIRAPROJECTS.put( "maven-scripting-plugin", "MSCRIPTING" );
        JIRAPROJECTS.put( "maven-shade-plugin", "MSHADE" );
        JIRAPROJECTS.put( "maven-shared-incremental", "MSHARED" );
        JIRAPROJECTS.put( "maven-shared-io", "MSHARED" );
        JIRAPROJECTS.put( "maven-shared-jar", "MSHARED" );
        JIRAPROJECTS.put( "maven-shared-resources", "MSHARED" );
        JIRAPROJECTS.put( "maven-shared-utils", "MSHARED" );
        JIRAPROJECTS.put( "maven-site", "MNGSITE" );
        JIRAPROJECTS.put( "maven-site-plugin", "MSITE" );
        JIRAPROJECTS.put( "maven-source-plugin", "MSOURCES" );
        JIRAPROJECTS.put( "maven-stage-plugin", "MSTAGE" );
        JIRAPROJECTS.put( "maven-surefire", "SUREFIRE" );
        JIRAPROJECTS.put( "maven-toolchains-plugin", "MTOOLCHAINS" );
        JIRAPROJECTS.put( "maven-verifier", "MSHARED" );
        JIRAPROJECTS.put( "maven-verifier-plugin", "MVERIFIER" );
        JIRAPROJECTS.put( "maven-wagon", "WAGON" );
        JIRAPROJECTS.put( "maven-war-plugin", "MWAR" );
        JIRAPROJECTS.put( "maven-wrapper-plugin", "MWRAPPER" );
    }


    @Override
    public String getOutputName()
    {
        return "dist-tool-branches";
    }

    @Override
    public String getName( Locale locale )
    {
        return "Dist Tool> List Branches";
    }

    @Override
    public String getDescription( Locale locale )
    {
        return "Shows the list of branches of every Git repository on one page";
    }

    @Override
    protected void executeReport( Locale locale )
        throws MavenReportException
    {
        Collection<String> repositoryNames;
        try
        {
            repositoryNames = repositoryNames();
        }
        catch ( IOException e )
        {
            throw new MavenReportException( "Failed to extract repositorynames from Gitbox", e );
        }
        
        List<Result> repoStatus = new ArrayList<>( repositoryNames.size() );
        
        Collection<String> included = repositoryNames.stream()
                                                     .filter( s -> !excluded.contains( s ) )
                                                     .collect( Collectors.toList() );
        
        for ( String repository : included )
        {
            final String gitboxHeadsUrl = gitboxUrl + "?p=" + repository + ".git;a=heads";
            final String repositoryJobUrl = mavenboxJobsBaseUrl + "job/" + repository;

            try
            {
                Document gitboxHeadsDoc = JsoupRetry.get( gitboxHeadsUrl );
                
                Element headsTable = gitboxHeadsDoc.selectFirst( "table.heads" );
                
                if ( headsTable == null )
                {
                    getLog().warn( "Ignoring " + repository );
                    continue;
                }
                
                Document jenkinsBranchesDoc = JsoupRetry.get( repositoryJobUrl );

                Result result = new Result( repository, repositoryJobUrl );
                int masterBranchesGit = 0;
                int masterBranchesJenkins = 0;
                int jiraBranchesGit = 0;
                int jiraBranchesJenkins = 0;
                int dependabotBranchesGit = 0;
                int dependabotBranchesJenkins = 0;
                int restGit = 0;
                int restJenkins = 0;
                
                for ( Element tableRow : headsTable.select( "tr" ) )
                {
                    String name = tableRow.selectFirst( "a.name" ).text();

                    if ( "master".equals( name ) )
                    {
                        masterBranchesGit++;
                        
                        if ( jenkinsBranchesDoc.getElementById( "job_master" ) != null )
                        {
                            masterBranchesJenkins++;
                        }
                    }
                    else if ( JIRAPROJECTS.containsKey( repository )
                        && name.toUpperCase().startsWith( JIRAPROJECTS.get( repository ) + '-' ) )
                    {
                        jiraBranchesGit++;
                        if ( jenkinsBranchesDoc.getElementById( URLEncoder.encode( "job_" + name, "UTF-8" ) ) != null )
                        {
                            jiraBranchesJenkins++;
                        }
                    }
                    else if ( name.startsWith( "dependabot/" ) )
                    {
                        dependabotBranchesGit++;
                        if ( jenkinsBranchesDoc.getElementById( URLEncoder.encode( "job_" + name, "UTF-8" ) ) != null )
                        {
                            dependabotBranchesJenkins++;
                        }
                    }
                    else
                    {
                        restGit++;
                        if ( jenkinsBranchesDoc.getElementById( URLEncoder.encode( "job_" + name, "UTF-8" ) ) != null )
                        {
                            restJenkins++;
                        }
                    }
                }
                result.setMasterBranchesGit( masterBranchesGit );
                result.setMasterBranchesJenkins( masterBranchesJenkins );
                result.setJiraBranchesGit( jiraBranchesGit );
                result.setJiraBranchesJenkins( jiraBranchesJenkins );
                result.setDependabotBranchesGit( dependabotBranchesGit );
                result.setDependabotBranchesJenkins( dependabotBranchesJenkins );
                result.setRestGit( restGit );
                result.setRestJenkins( restJenkins );

                repoStatus.add( result );
            }
            catch ( IOException e )
            {
                getLog().warn( "Failed to read status for " + repository + " Jenkins job " + repositoryJobUrl  );
            }
        }
        
        generateReport( repoStatus );
    }
    
    private void generateReport( List<Result> repoStatus )
    {
        Sink sink = getSink();
        
        sink.head();
        sink.title();
        sink.text( "List Branches" );
        sink.title_();
        sink.head_();
        
        sink.body();
        sink.paragraph();
        sink.rawText( "Values are shown as <code>jenkinsBranches / gitBranches</code>, "
            + "because not all branches end up in Jenkins, this depends on the existence of the JenkinsFile." );
        sink.paragraph();

        sink.table();
        sink.tableRow();
        sink.tableHeaderCell();
        sink.text( "Jenkins job / GitHub" );
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text( "master" );
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text( "JIRA branches" );
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text( "Dependabot Branches" );
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text( "Rest" );
        sink.tableHeaderCell_();
        sink.tableHeaderCell();
        sink.text( "Total" );
        sink.tableHeaderCell_();
        sink.tableRow_();
        
        repoStatus.stream()
            .sorted( Comparator.comparing( Result::getTotalJenkins ).thenComparing( Result::getTotalGit ).reversed() )
            .forEach( r -> 
            {
                sink.tableRow();
                
                sink.tableCell();
                sink.link( r.getBuildUrl() );
                sink.rawText( r.getRepositoryName() );
                sink.link_();
                sink.rawText( " / " );
                sink.link( "https://github.com/apache/" + r.getRepositoryName() );
                sink.rawText( r.getRepositoryName() );
                sink.link_();
                sink.tableCell_();

                // master
                sink.tableCell();
                sink.text( r.getMasterBranchesJenkins() + " / " + r.getMasterBranchesGit()  );
                sink.tableCell_();

                //jira branches
                sink.tableCell();
                if ( r.getJiraBranchesGit() == 0 ) 
                {
                    sink.text( "0 / 0" );
                }
                else
                {
                    sink.bold();
                    sink.text( r.getJiraBranchesJenkins() + " / " + r.getJiraBranchesGit() );
                    sink.bold_();
                }
                sink.tableCell_();

                // dependabot branches
                sink.tableCell();
                if ( r.getDependabotBranchesGit() == 0 ) 
                {
                    sink.text( "0 / 0" );
                }
                else
                {
                    sink.bold();
                    sink.text( r.getDependabotBranchesJenkins() + " / " + r.getDependabotBranchesGit() );
                    sink.bold_();
                }
                sink.tableCell_();

                // rest
                sink.tableCell();
                if ( r.getRestGit() == 0 ) 
                {
                    sink.text( "0 / 0" );
                }
                else
                {
                    sink.bold();
                    sink.text( r.getRestJenkins() + " / " + r.getRestGit() );
                    sink.bold_();
                }
                sink.tableCell_();
                
                // total
                sink.tableCell();
                sink.text( r.getTotalJenkins() + " / " + r.getTotalGit() );
                sink.tableCell_();
                
                sink.tableRow_();
            } );
        
        sink.table_();
        sink.body_();
    }

    /**
     * Extract Git repository names for Apache Maven from
     * <a href="https://gitbox.apache.org/repos/asf">Gitbox main page</a>.
     *
     * @return the list of repository names (without ".git")
     * @throws IOException problem with reading repository index
     */
    protected Collection<String> repositoryNames()
        throws IOException
    {
        List<String> names = new ArrayList<>( 100 );
        Document doc = JsoupRetry.get( gitboxUrl );
        // find Apache Maven table
        Element apacheMavenTable = doc.getElementsMatchingText( "^Apache Maven$" ).parents().get( 0 );

        Elements gitRepo = apacheMavenTable.select( "tbody tr" ).not( "tr.disabled" ).select( "td:first-child a" );

        for ( Element element : gitRepo )
        {
            names.add( element.text().split( "\\.git" )[0] );
        }

        return names;
    }
}