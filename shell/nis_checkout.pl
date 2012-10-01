#!/usr/bin/perl

use strict;


################################################################################
####                                                                           #
#### Main Program                                                              #
####                                                                           #
################################################################################

## Script to checkout a PASTA web services and its dependent directories from
## the NIS repository to a development or production server.

my %pastaWebServices = (
  'default' =>            'ant-util,cobertura,common,db-util,doc-util,lib',
  'AuditManager'       => 'default',
  'DataPackageManager' => 'default',
  'DataPackageManager-0.1' =>   '',
  'DataPackageManager-0.2' =>   '',
  'DataPortal'  =>        'default',
  'EventManager' =>       'default',
  'EventManager-0.1' =>   '',
  'EventManager-0.2' =>   '',
  'Gatekeeper' =>         'default',
  'Gatekeeper-0.1' =>   '',
  'acceptancetest' =>     'default',
  'common' =>             'ant-util,cobertura,doc-util,lib',
  'eventmanagertester' => 'default',
  'gatekeepertester' =>   'default',
  'tokenmaker' =>         'default'
);

## Parse the passwords from the command-line arguments
##
my ($serviceName, $includeList, $excludeList) = checkArguments();

my @includes = split(',', $includeList);
my @excludes = split(',', $excludeList);


my $localSvnDir = "/home/pasta/svn";
my $shellDir = "${localSvnDir}/shell";
my $configFile = "$shellDir/pasta-web-services.config";

my $checkout;
my @checkoutList;
my $defaultDependency;
my $defaultDependencies = $pastaWebServices{'default'};
my @defaultDependenciesList = split(',', $defaultDependencies);
my $dependency;
my $dependencies;
my @dependenciesList;
my $environmentVariable;
my $majorVersion;
my $minorVersion;
my $tags = 0;


if (defined $pastaWebServices{$serviceName}) {
  if ($serviceName eq 'default') {
    print(STDERR "***Error: 'default' is not a valid service name.\n");
    usage();
    exit(1);
  } 
  else {
    push(@checkoutList, $serviceName);
  }

  $dependencies = $pastaWebServices{$serviceName};
  @dependenciesList = split(',', $dependencies);

  foreach $dependency (@dependenciesList) {
    if ($dependency eq 'default') {
      foreach $defaultDependency (@defaultDependenciesList) {
        push(@checkoutList, $defaultDependency);
      }
    }
    else {
      push(@checkoutList, $dependency);
    }
  }
}
else {
  print(STDERR "***Error: Unknown pasta web service name: $serviceName.\n");
  exit(1);
}

$environmentVariable = uc($serviceName);

if ($serviceName =~ m|(\w+)-(\d)\.(\d)$|) {
  $tags = 'true';
  $serviceName = $1;
  $majorVersion = $2;
  $minorVersion = $3;
  $environmentVariable = uc($serviceName);
  $environmentVariable .= "_${majorVersion}${minorVersion}";
}

foreach $checkout (@checkoutList) {
  print(STDERR "\nChecking out $checkout:\n");
  svnCheckout($serviceName, $checkout, $tags);
}


################################################################################
####                                                                           #
#### Subroutines                                                               #
####                                                                           #
################################################################################


################################################################################
###
### Subroutine:  checkArguments
###
### Description: Check that the user has specified the service name
###              and either the -include or -exclude options.
###
### Arguments:   None.
###
### Returns:     List of three values:
###
###              $serviceName      The value of $ARGV[0]
###
###              $includeList     The value following the -include option
###
###              $excludeList     The value following the -exclude option
###
################################################################################

sub checkArguments {
    my $excludeList = "";
    my $includeList = "";
    my $serviceName = "";

    if ((@ARGV == 5)) {
      if (($ARGV[1] =~ m/^-inc/i) && ($ARGV[3] =~ m/^-exc/i)) {
        $includeList = $ARGV[2];
        $excludeList = $ARGV[4];
      }
      elsif (($ARGV[3] =~ m/^-inc/i) && ($ARGV[1] =~ m/^-exc/i)) {
        $includeList = $ARGV[4];
        $excludeList = $ARGV[2];
      }
    }
    elsif (@ARGV == 3) {
      if ($ARGV[1] =~ m/^-inc/i) {
        $includeList = $ARGV[2];
      }
      elsif ($ARGV[1] =~ m/^-exc/i) {
        $excludeList = $ARGV[2];
      }
    }
    elsif (@ARGV == 1) {
    }
    else {
        usage();
        exit(0);
    }

    if ($ARGV[0] =~ m/^\w+/i) {
      $serviceName = $ARGV[0];
    }
    else {
        usage();
        exit(0);
    }

    return ($serviceName, $includeList, $excludeList);
}


################################################################################
###
### Subroutine:  svnCheckout
###
### Description: Checks out a service or directory from the NIS subversion repository
###
### Arguments:   $serviceName  Name of the service to checkout
###              $checkout     Name of the directory to checkout under the service
###              $tags 'true' if this is a checkout from the tags area, else 0
###
### Returns:     None.
###
################################################################################

sub svnCheckout {
  my ($serviceName, $checkout, $tags) = @_;

  my $checkoutRoot = "${localSvnDir}";
  my $checkoutPath;
  my $systemCommand = "svn checkout ";
  my $repositoryRoot = "https://svn.lternet.edu/svn/NIS";
  my $repositoryPath;
  my $status;

  if ($tags) {
    $repositoryRoot .= "/tags";
  }
  else {
    $repositoryRoot .= "/trunk";
    $checkoutRoot .= "/${serviceName}";
  }

  $repositoryPath = "${repositoryRoot}/${checkout}";
  $checkoutPath = "${checkoutRoot}/${checkout}";

  $systemCommand .= "$repositoryPath $checkoutPath";
  print(STDERR "  $systemCommand\n");
  $status = system($systemCommand);
  
  if ($status) {
    print(STDERR "***Error: Encountered error checking out ${serviceName}.");
  }
}


###############################################################################
###
### Subroutine:  usage
###
### Description: Prints usage information to STDERR.
###
### Arguments:   None.
###
### Returns:     None.
###
###############################################################################

sub usage {
    print(STDERR "\n");
    print(STDERR "nis_checkout.pl:  Script to checkout PASTA web services for deployment.\n");
    print(STDERR "\n");
    print(STDERR "    Usage:  nis_checkout <webService> [-include <includeList>] [-exclude <excludeList>]\n");
    print(STDERR "\n");
    print(STDERR "    ***NOTE***: The following two options are not yet implemented.\n");
    print(STDERR "    Options:  -include <includeList>  List of additional directories to include.\n");
    print(STDERR "              -exclude <excludeList>  List of directories to explicitly exclude.\n");
    print(STDERR "\n");
    print(STDERR "    Example:  perl nis_checkout.pl AuditManager -exclude lib\n");
}
