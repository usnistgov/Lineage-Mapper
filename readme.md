# Lineage Mapper

Welcome to Lineage Mapper - the National Institute of Standards and Technology's Cell Tracking application, developed by the Information Technology Laboratory-Software and Systems Division at NIST Gaithersburg.

We developed an open source, highly accurate, overlap-based cell tracking system that tracks live cells across a set of time-lapse images. The processing pipeline of the Lineage mapper is shown in Figure 1. The Lineage Mapper successfully detects dynamic single cell behavior: cell migration, changes in cell state (mitosis, apoptosis); cells within colonies or the entire colonies, cells within cell sheets or cells moving around with high cell-cell contact.

![Lineage Mapper Pipeline](../../wiki/imgs/Lineage_Mapper_Pipeline.png)

Figure 1: Lineage Mapper processing pipeline and tracking outputs. The algorithmic steps consists of: (1) compute cost between cells from consecutive frames, (2) detect cell collision and account for it, (3) detect mitosis events, (4) assign tracks between cells, and (5) create tracking outputs. The outputs includes saved tracked images, the cell lineage plotting and 4 tracking output measurements: (1) confidence index, (2) the birth and death matrix, (3) the mitosis matrix, (4) the fusion matrix.

This repository contains source code for the plugin in one branch and the source code for the MATLAB prototype in another.

# Quick Navigation

#### - [About Lineage Mapper](https://isg.nist.gov/deepzoomweb/resources/csmet/pages/cell_tracking/cell_tracking.html)
#### - [Wiki](https://github.com/NIST-ISG/Lineage-Mapper/wiki)
#### - [User Guide](https://github.com/NIST-ISG/Lineage-Mapper/wiki/User-Guide)
