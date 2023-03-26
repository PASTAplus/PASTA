============================
Python Development Protocols
============================

Introduction
------------

For the past 7 years, the Environmental Data Initiative (EDI) has embraced the
Python programming paradigm for its simplicity and ease of use. For all of its
virtues, Python still comes with some ugly baggage: package management and
`virtual environments <https://docs.python.org/3/tutorial/venv.html>`_ have
been confusing and daunting for the weary software developer. The two most
common Python package management applications are `Pip
<https://pypi.org/project/pip/>`_ and `Conda
<https://docs.conda.io/en/latest/>`_, while applications like `Conda
<https://docs.conda.io/en/latest/>`_, `Pipenv
<https://pipenv.pypa.io/en/latest/>`_, `Pyenv
<https://github.com/pyenv/pyenv#readme>`_, and `Venv
<https://docs.python.org/3/library/venv.html>`_ (Python language default)
provide variations for managing virtual environments. EDI has adopted **Conda**
as its de facto Python package manager (see `here
<https://www.anaconda.com/blog/understanding-conda-and-pip>`_ how Conda differs
from Pip) and virtual environment tool for the following reasons [#pip_benefits]_\ :

#. Conda provides a consistent development environment across all major operating systems without shims or monkey patching - it works out of the box. This was a critical factor in the early days when Microsoft did not support Python as well as it does today.
#. Through the `Anaconda <https://repo.anaconda.com/>`_ package repository, Conda maintains binary versions of all Python packages optimized for the operating system they are deployed to. Pip, which defaults to `Pypi.org <https://pypi.org/>`_ for Python packages, does not require binary distributions (*bdist*) and can lead to installation and build errors for software-only (*sdist*) packages.
#. Conda enforces strict dependency management to meet the developer’s installed Python package requirements. Pip does not enforce dependency management at scale, as does Conda.

TLDR - Takeaways
----------------

#. **You should** use Conda virtual environments over other Python virtual environments.
#. **You should** use Conda packages, when available, over those installed using Pip.
#. **You must** manually add any Pip-installed package into the environment-min.yml file (see below).
#. **You should** export both environment.yml and environment-min.yml files from Conda as part of the project. **You should** also generate a Pip-based requirements.txt file to be part of the project (see caveats below).
#. If using JetBrains PyCharm (or other IDE), **you should** ignore any recommendations to install missing packages as declared in the requirements.txt file using Pip; you should, however, use the recommendation as an indication that your virtual environment is not correctly configured.

Working with Conda
------------------

Installation
++++++++++++

The first step is `downloading Conda <https://conda.io/projects/conda/en/stable/user-guide/install/download.html>`_ and installing it on your local computer. Conda comes in `two versions <https://conda.io/projects/conda/en/stable/user-guide/install/download.html#anaconda-or-miniconda>`_\ : a full-featured application bundled with *Anaconda* or a streamlined application called *Miniconda*. Both versions contain Conda and will provide identical functionality. When installing Conda, the installation script will ask you to decide if you want the Conda "base environment" to begin immediately when you login into your account or if you prefer to start it when required.

Depending on your operating system and the command line shell you are using, you may see an indication that the base environment is active within the command line prompt line: ::

  (base) pasta@sue:~$

Conda help
++++++++++

Conda supports extensive help and can be accessed using the --help optional argument. For example: ::

  conda env --help

Creating and activating a Conda virtual environment
+++++++++++++++++++++++++++++++++++++++++++++++++++

Before creating your first Python module or even initializing your Git repository, you should generate a working Python virtual environment. In short, a Python virtual environment isolates your Python project from other Python projects you may have on your system. It limits the possibility of conflicts with like-named packages or the same packages with different versions. If uncertain, create a virtual environment and use it.

To create a Conda virtual environment, you must supply the name of the environment to be created with the name parameter. Conda supports many optional parameters when creating a virtual environment. There are two that I generally use: no-default-packages and the Python version to be used in the virtual environment. For example: ::

  conda create --name my_env --no-default-packages python=3.10

In this example, Conda has created a new Python 3.10 virtual environment with the "my_env" name and without any additional Python packages installed. To activate this Conda virtual environment and begin using it, you execute the following: ::

  conda activate my_env

The command line prompt will now change from base to my_env: ::

  (my_env) pasta@sue:~$

To deactivate the current Conda virtual environment, you would use the Conda deactivate command: ::

  conda deactivate

To remove a Conda virtual environment, you would use the Conda env remove command: ::

  conda env remove --name my_env

Listing Python packages in the Conda virtual environment
++++++++++++++++++++++++++++++++++++++++++++++++++++++++

To see the existing Python packages within a Conda virtual environment, you would use the list command: ::

  conda list
  # packages in environment at /home/servilla/anaconda3/envs/emlvp:
  #
  # Name                    Version                   Build  Channel
  _libgcc_mutex             0.1                 conda_forge    conda-forge
  _openmp_mutex             4.5                       2_gnu    conda-forge
  astroid                   2.14.1          py310hff52083_0    conda-forge
  attrs                     22.2.0             pyh71513ae_0    conda-forge
  black                     23.1.0          py310hff52083_0    conda-forge
  bzip2                     1.0.8                h7f98852_4    conda-forge
  ca-certificates           2022.12.7            ha878542_0    conda-forge
                                                                     ...

This list of information includes the package name (as installed by Conda), the
package version, the specific build version within the Anaconda package
repository, and the source channel within Anaconda (generally either *defaults*
or *conda-forge*) from which the package was installed. To see information
about a  specific package, use the package name as an argument with the list
command: ::

  conda list astroid
  # packages in environment at /home/servilla/anaconda3/envs/emlvp:
  #
  # Name                    Version                   Build  Channel
  astroid                   2.14.1          py310hff52083_0    conda-forge

It is important to note that the Conda *list* command will display information about Python packages that were installed with Pip alongside those installed with Conda. For example, the lxml package in this virtual environment was installed with Pip: ::

  # Name                    Version                   Build  Channel
  lxml                      4.9.2                    pypi_0    pypi

You can see a similar but compressed listing of Python packages, including
those installed with Pip, within the virtual environment using the Conda *env
export* command: ::

  conda env export
  name: ezeml
  channels:
    - conda-forge
    - defaults
  dependencies:
    - _libgcc_mutex=0.1=conda_forge
    - _openmp_mutex=4.5=2_gnu
    - alembic=1.8.1=py38h06a4308_0
    - asn1crypto=1.5.1=py38h06a4308_0
    - attrs=22.2.0=pyh71513ae_0
    - bottleneck=1.3.5=py38h7deecbd_0
    - brotlipy=0.7.0=py38h27cfd23_1003
                                   ... 
    - pip:
        - lxml==4.9.2

This format will be discussed in greater detail below.
Searching for and installing Python packages into a Conda virtual environment
To search for a particular Python package in the Anaconda package repository, you can pass the package name to the conda *search* command: ::

  conda search lxml
  Loading channels: done
  # Name                       Version           Build  Channel             
  lxml                           3.6.2          py27_0  conda-forge         
  lxml                           3.6.2          py27_1  conda-forge         
  lxml                           3.6.2          py34_0  conda-forge         
  lxml                           3.6.2          py34_1  conda-forge
                                                              ...


You can also search for a specific package version or a range of versions: ::

  conda search lxml=4.8.0

or ::

  conda search "lxml>=4.8"

Note that the argument in the last example was enclosed by quotes since many command line shell environments interpret the greater-than sign differently.

Installing a new Python package into an existing Conda virtual environment is straightforward with the Conda *install* command: ::

  conda install lxml

You can also request Conda to install a specific version of the package: ::

  conda install lxml=4.9.2

You should know that by installing a specific (or "pinned") version of a Python package, you may affect other packages already in the virtual environment due to related dependencies. In other words, installing a specific version may cause a dependency resolution chain effect that will likely take a long time to resolve or ultimately fail to satisfy all dependencies. When dependencies cannot be resolved, the package installation does not occur.

There are times when you want to install a particular Python package that is not in the *Anaconda* package repository but is in Pypi.org or on your local computer. In these cases, it is generally acceptable to perform the installation with Pip: ::

  pip install weird_package

or ::

  pip install /path/to/setup.py/file

Pip will perform a cursory inspection for dependencies of the new Python package and may install them as part of the original package installation if they are not already installed by Conda. IF AT ALL POSSIBLE, IT IS BEST TO INSTALL PYTHON PACKAGES USING CONDA SINCE THEY WILL HAVE BEEN OPTIMIZED WITHIN THE ANACONDA PACKAGE REPOSITORY, AND CONDA PERFORMS A MORE COMPREHENSIVE DEPENDENCY ANALYSIS. IN ADDITION, CONDA TRACKS YOUR PACKAGE INSTALLATION, WHICH CAN BE REVIEWED USING THE CONDA ENV EXPORT COMMAND.

Lastly, you can also remove Python packages previously installed with Conda. The Conda *remove* command ::

  conda remove lxml

will remove the single lxml package, whereas ::

  conda remove lxml requests weird_package

will remove all three packages from the active virtual environment. It is
important to note that Conda will also remove any packages that depend on the
removed package unless an alternative dependency can be found.

Development to Production workflows using Conda
-----------------------------------------------

Python projects in EDI generally begin on local workstations (laptop/desktop computers) and are eventually deployed to production servers running in the EDI-CARC or EDI-CERIA compute environments. EDI utilizes virtual client machines running the server version of the Ubuntu operating system in these environments. Ubuntu is a derivative of Debian Linux. A key component of this workflow is replicating the necessary packages from the workstation to the server. With this in mind, a typical deployment from a workstation to a server involves several steps:

  
1. Generate a working project on a local workstation, including

  a. Create Conda virtual environment
  b. Initialize Git
  c. Add some packages
  d. Add some new code
  e. Repeat b and c multiple times
  f. Capture the Conda environment for replication on server
  g. Git things up and push to GitHub
  h. Repeat c through g

2. On server

  a. Clone GitHub project
  b. Create Conda virtual environment
  c. Deploy application
  d. Run application

3. On server, if new code is pushed to GitHub

  a. Pull code from GitHub
  b. Rebuild virtual environment if new packages installed
  c. Deploy application
  d. Run application

The crucial steps in this workflow are 1f, 2b, and 3b. These steps allow the
server to operate an equivalent Conda virtual environment to the one on the
workstation so that the application functions as expected - equivalent, not
necessarily an exact replica. The components of the Conda virtual environment
are captured using the Conda *env export* command (step 1f). There are two
variants of this command that are critical. The first captures a complete
snapshot of all packages that were installed in the virtual environment,
including those installed as dependencies to other packages and those that were
installed with Pip. The second variant only captures the packages you installed
when using the Conda *install* command. Both variants use the same Conda env
export command but have different optional parameters. The first variant uses
the *--no-builds* option, while the second variant uses the *--from-history*
option. The *--no-builds* option tells Conda to leave off the specific build
version, which mitigates having to match CPU-specific builds. Both variants
will use the *--file* option to write the output from the environment export to
a specific file using a YAML format. EDI uses the following convention for the
environment export files: the first variant (full package listing
and *--no-builds*) writes to *environment.yml*. The second variant
(minimal package listing and *--from-history*) writes to *environment-min.yml*.
The commands are as follows: ::

  conda env export --no-builds --file environment.yml

and ::

  conda env export --from-history --file environment-min.yml

Steps 2b and 3b are performed with a modified version of
the *environment-min.yml* file, edited to include any Pip-installed packages.
You will find the Pip-installed packages within the *environment.yml* file. It
is the modified *environment-min.yml* file that is used to create or update the
Conda virtual environment on the server because it contains no information
about package dependencies, which may be operating system specific. This way,
Conda only needs to know about packages that are directly relevant to the
application; dependencies will be installed indirectly and correctly aligned to
the server operating system.

For example, a *scratch* project is created with *Python 3.10* as the only 
initial requirement. We install the ``click``, ``lxml``, ``daiquiri``, and
``python-json-logger`` packages from Conda. We also install emlvp from Pypi.org
using Pip. Next, we generate both the environment.yml and environment-min.yml
files, then modify the *environment-min.yml* file to include the emlvp package
installed using Pip. Finally, everything, including the environment files, is
committed to Git and pushed to GitHub for server deployment. ::

  conda create --name scratch --no-default-packages python=3.10
  conda install click lxml daiquiri python-json-logger
  pip install emlvp
  conda env export --no-builds --file environment.yml
  conda env export --from-history --file environment-min.yml

The *environment.yml* file contains the following (content truncated in
middle): ::

  name: scratch
  channels:
    - conda-forge
    - defaults
  dependencies:
    - ca-certificates=2022.12.7
    - click=8.1.3
    - daiquiri=3.0.0
    - icu=70.1
           ...
    - tzdata=2022g
    - wheel=0.40.0
    - xz=5.2.6
    - pip:
        - emlvp==0.0.5
  prefix: /home/servilla/anaconda3/envs/scratch

The non-modified *environment-min.yml* contains only the Python packages that
you installed with Conda: ::

  name: scratch
  channels:
    - conda-forge
    - defaults
  dependencies:
    - python=3.10
    - python-json-logger
    - click
    - daiquiri
    - lxml
  prefix: /home/servilla/anaconda3/envs/scratch

The modified *environment-min.yml* contains the following, including the Pip
installation of emlvp: ::

  name: scratch
  channels:
    - conda-forge
    - defaults
  dependencies:
    - python=3.10
    - python-json-logger
    - click
    - daiquiri
    - lxml
    - pip
    - pip:
        - emlvp
  prefix: /home/servilla/anaconda3/envs/scratch

Note that *pip* must be explicitly listed as a dependency in the
*environment-min.yml* file before it can actually install *emlvp*.

On the server, Conda *env create* or *env update* is used to either create the 
Conda virtual environment or to update the virtual environment: ::

  conda env create --file environment-min.yml

or ::

  conda env update --file environment-min.yml

Once created, the Conda virtual environment on the server can be activated and
used as it is used on the workstation.

Pip Requirements
++++++++++++++++

In addition to the Conda environment files, a *Pip*-generated
*requirements.txt* file is created and added to the Git repository. This file
provides GitHub information to its Dependabot screening for packages that have
security issues, and it allows non-Conda users a way to create a virtual
environment using other means. This file is only a convenience and should not
generally be used for anything EDI Python related. ::

  pip list --format freeze > requirements.txt

Note that many IDE applications, including *JetBrains PyCharm*, will use requirements.txt to determine the fixity of a virtual environment. In cases when the specifications in a requirements.txt do not match the current state of the virtual environment, the IDE will offer to update the virtual environment with missing or outdated packages. You should avoid allowing the IDE to perform any virtual environment update since it will likely use Pip to perform the update, not Conda. You should, however, take heed of any indication from the IDE (or otherwise) that the virtual environment is not correctly configured.

Conclusion
----------

Although using Conda as EDI’s Python package manager and virtual environment tool comes with some effort, the diligence in following a strict workflow for developing applications on workstations makes deploying on servers much easier and more effective.

.. [#pip_benefits] Pip does, however, provide flexibility in package
                   installation: Pypi.org contains Python packages from a far greater number of developers than Conda; Pip can install Python packages from GitHub or a local development project, in addition to Pypi.org; and Conda may lag behind in updates, which can be an issue for security reasons.

.. toctree::
    :hidden:
